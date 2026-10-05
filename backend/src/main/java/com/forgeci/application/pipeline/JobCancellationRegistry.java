package com.forgeci.application.pipeline;
import java.util.UUID;import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.ConcurrentMap;import org.springframework.stereotype.Component;
@Component public class JobCancellationRegistry{
 private final ConcurrentMap<UUID,Thread> workers=new ConcurrentHashMap<>();private final ConcurrentMap<UUID,Process> processes=new ConcurrentHashMap<>();
 public void register(UUID jobId){workers.put(jobId,Thread.currentThread());}
 public void registerProcess(UUID jobId,Process process){processes.put(jobId,process);}
 public void unregister(UUID jobId){workers.remove(jobId);processes.remove(jobId);}
 public void cancel(UUID jobId){Process p=processes.get(jobId);if(p!=null&&p.isAlive())p.destroyForcibly();Thread t=workers.get(jobId);if(t!=null)t.interrupt();}
}