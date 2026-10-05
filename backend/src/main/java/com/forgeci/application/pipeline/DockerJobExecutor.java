package com.forgeci.application.pipeline;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DockerJobExecutor implements JobExecutor {
    private final String dockerBinary;
    private final WorkspaceManager workspaces;
    private final LogChunkService logs;

    public DockerJobExecutor(@Value("${forgeci.worker.docker-binary:docker}") String dockerBinary, WorkspaceManager workspaces, LogChunkService logs) {
        this.dockerBinary=dockerBinary; this.workspaces=workspaces; this.logs=logs;
    }

    @Override public ExecutionResult execute(JobExecutionRequest request) {
        Path workspace=workspaces.checkout(request.cloneUrl(),request.commitSha(),request.githubToken());
        try {
            List<String> args=new ArrayList<>(List.of(dockerBinary,"run","--rm","--network","none","--read-only",
                    "--cap-drop","ALL","--security-opt","no-new-privileges","--pids-limit",String.valueOf(request.pidsLimit()),
                    "--cpus",String.valueOf(request.cpuLimit()),"--memory",String.valueOf(request.memoryBytes()),
                    "--tmpfs","/tmp:rw,noexec,nosuid,size=64m","-v",workspace.toAbsolutePath()+":/workspace:rw",
                    "-w","/workspace","--label","forgeci.job="+request.jobId(),"--label","forgeci.commit="+request.commitSha(),
                    request.image(),"sh","-lc",request.command()));
            Process process=new ProcessBuilder(args).redirectErrorStream(false).start();
            ExecutorService io=Executors.newFixedThreadPool(2); AtomicLong sequence=new AtomicLong(logs.nextSequence(request.jobId()));
            Future<?> out=io.submit(()->capture(process.getInputStream(),request.jobId(),"stdout",sequence));
            Future<?> err=io.submit(()->capture(process.getErrorStream(),request.jobId(),"stderr",sequence));
            try {
                if(!process.waitFor(request.timeout().toMillis(),TimeUnit.MILLISECONDS)){process.destroyForcibly();return new ExecutionResult(124,true);}
                out.get(5,TimeUnit.SECONDS);err.get(5,TimeUnit.SECONDS);return new ExecutionResult(process.exitValue(),false);
            } finally {io.shutdownNow();}
        } catch(IOException e){throw new IllegalStateException("Unable to start container runtime",e);}
          catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Worker interrupted",e);}
          catch(ExecutionException|TimeoutException e){throw new IllegalStateException("Unable to capture container output",e);}
        finally {workspaces.cleanup(workspace);}
    }

    private void capture(InputStream input,UUID jobId,String stream,AtomicLong sequence){
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8))){
            String line;while((line=reader.readLine())!=null)logs.append(jobId,stream,sequence.getAndIncrement(),line+System.lineSeparator());
        }catch(IOException e){logs.append(jobId,stream,sequence.getAndIncrement(),"[log capture failed]"+System.lineSeparator());}
    }
}
