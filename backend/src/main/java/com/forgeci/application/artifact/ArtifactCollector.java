package com.forgeci.application.artifact;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;
import org.springframework.stereotype.Service;
@Service
public class ArtifactCollector{
 private static final int MAX_FILES=1000; private static final long MAX_BYTES=500L*1024*1024;
 public Optional<Path> collect(Path workspace,List<String> patterns)throws IOException{
  if(patterns==null||patterns.isEmpty())return Optional.empty();
  Path root=workspace.toAbsolutePath().normalize();if(!Files.isDirectory(root))throw new IllegalArgumentException("Workspace does not exist");
  List<Path> matches=new ArrayList<>();
  for(String raw:patterns){
   if(raw==null||raw.isBlank()||raw.startsWith("/")||raw.startsWith("\\")||Path.of(raw).normalize().startsWith(".."))throw new IllegalArgumentException("Unsafe artifact path");
   PathMatcher matcher=FileSystems.getDefault().getPathMatcher("glob:"+raw);
   try(var stream=Files.walk(root)){
    stream.filter(Files::isRegularFile).forEach(p->{Path rel=root.relativize(p);if(matcher.matches(rel))matches.add(p);});
   }
  }
  matches=matches.stream().distinct().sorted().limit(MAX_FILES).toList();if(matches.isEmpty())return Optional.empty();
  long total=0;Path zip=Files.createTempFile("forgeci-artifacts-",".zip");
  try(ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(zip))){
   for(Path file:matches){long size=Files.size(file);if(size>MAX_BYTES-total)throw new IllegalArgumentException("Artifact collection exceeds 500 MiB");
    String name=root.relativize(file).toString().replace(File.separatorChar,'/');
    out.putNextEntry(new ZipEntry(name));try(InputStream in=Files.newInputStream(file)){in.transferTo(out);}out.closeEntry();total+=size;
   }
  }catch(RuntimeException|IOException e){Files.deleteIfExists(zip);throw e;}
  return Optional.of(zip);
 }
}