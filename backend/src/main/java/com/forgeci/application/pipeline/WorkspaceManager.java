package com.forgeci.application.pipeline;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceManager {
    private final String gitBinary;
    private final com.forgeci.application.security.OutboundUrlPolicy outboundUrls;

    public WorkspaceManager(@Value("${forgeci.worker.git-binary:git}") String gitBinary, com.forgeci.application.security.OutboundUrlPolicy outboundUrls){this.gitBinary=gitBinary;this.outboundUrls=outboundUrls;}

    public Path checkout(String cloneUrl,String commitSha,String token){
        validateCloneUrl(cloneUrl);
        if(token==null||token.isBlank())throw new IllegalArgumentException("GitHub token is required for checkout");
        try{
            Path workspace=Files.createTempDirectory("forgeci-workspace-");
            run(workspace.getParent(),"init",workspace.toString());
            run(workspace,"remote","add","origin",cloneUrl);
            ProcessBuilder fetch=new ProcessBuilder(gitBinary,"fetch","--depth","1","origin",commitSha);
            fetch.directory(workspace.toFile()).redirectErrorStream(true);
            fetch.environment().put("GIT_CONFIG_COUNT","1");
            fetch.environment().put("GIT_CONFIG_KEY_0","http.extraHeader");
            fetch.environment().put("GIT_CONFIG_VALUE_0","Authorization: Bearer "+token);
            Process process=fetch.start();
            if(!process.waitFor(60,TimeUnit.SECONDS)){process.destroyForcibly();throw new IllegalStateException("Git fetch timed out");}
            if(process.exitValue()!=0)throw new IllegalStateException("Unable to fetch requested commit");
            run(workspace,"checkout","--detach",commitSha);
            return workspace;
        }catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Workspace checkout interrupted",e);}
        catch(IOException e){throw new IllegalStateException("Unable to create repository workspace",e);}
    }

    public void cleanup(Path workspace){
        if(workspace==null)return;
        try(var stream=Files.walk(workspace)){stream.sorted(Comparator.reverseOrder()).forEach(path->{try{Files.deleteIfExists(path);}catch(IOException ignored){}});}catch(IOException ignored){}
    }

    private void run(Path directory,String... args)throws IOException,InterruptedException{
        var command=new java.util.ArrayList<String>(); command.add(gitBinary); java.util.Collections.addAll(command,args);
        Process process=new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        if(!process.waitFor(60,TimeUnit.SECONDS)){process.destroyForcibly();throw new IllegalStateException("Git command timed out");}
        if(process.exitValue()!=0)throw new IllegalStateException("Git command failed");
    }

    private void validateCloneUrl(String cloneUrl){
        try{
            var uri=java.net.URI.create(cloneUrl); outboundUrls.validate(uri); String host=uri.getHost();
            if(!"github.com".equalsIgnoreCase(host)&&!"www.github.com".equalsIgnoreCase(host))throw new IllegalArgumentException("Only GitHub repositories are supported");
        }catch(IllegalArgumentException e){throw new IllegalArgumentException("Invalid repository clone URL",e);}
    }
}
