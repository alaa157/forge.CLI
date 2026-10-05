package com.forgeci.application.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private final Path root;
    private final long maxBytes;
    private final Duration ttl;

    public CacheService(
            @Value("${forgeci.cache.root:${java.io.tmpdir}/forgeci-cache}") String root,
            @Value("${forgeci.cache.max-bytes:1073741824}") long maxBytes,
            @Value("${forgeci.cache.ttl-hours:168}") long ttlHours) {
        this.root = Path.of(root).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        this.ttl = Duration.ofHours(ttlHours);
    }

    public void restore(String namespace, String key, Path workspace, List<String> paths) {
        if (key == null || key.isBlank() || paths == null || paths.isEmpty()) return;
        Path archive = archive(namespace, key);
        if (!Files.isRegularFile(archive) || expired(archive)) return;
        try {
            try (InputStream in = new GZIPInputStream(Files.newInputStream(archive))) {
                CacheArchive.extract(in, workspace, paths);
            }
            Files.setLastModifiedTime(archive, FileTime.from(Instant.now()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to restore cache", e);
        }
    }

    public void save(String namespace, String key, Path workspace, List<String> paths) {
        if (key == null || key.isBlank() || paths == null || paths.isEmpty()) return;
        try {
            Files.createDirectories(root.resolve(namespace).normalize());
            Path archive = archive(namespace, key);
            Path temp = Files.createTempFile(root, "cache-", ".tmp");
            try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(temp))) {
                CacheArchive.write(out, workspace, paths, maxBytes);
            }
            Files.move(temp, archive, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save cache", e);
        }
    }

    private boolean expired(Path file) throws IOException {
        return Files.getLastModifiedTime(file).toInstant().plus(ttl).isBefore(Instant.now());
    }

    private Path archive(String namespace, String key) {
        String safeNamespace = safe(namespace);
        String digest;
        try {
            digest = hex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
        return root.resolve(safeNamespace).resolve(digest + ".tar.gz").normalize();
    }

    private static String safe(String value) {
        if (value == null || !value.matches("[A-Za-z0-9._-]{1,120}")) throw new IllegalArgumentException("Invalid cache namespace");
        return value;
    }

    private static String hex(byte[] b) {
        StringBuilder s=new StringBuilder();
        for(byte x:b)s.append(String.format("%02x",x));
        return s.toString();
    }

    static final class CacheArchive {
        private CacheArchive() {}
        static void write(OutputStream out, Path root, List<String> paths, long limit) throws IOException {
            java.util.zip.ZipOutputStream zip=new java.util.zip.ZipOutputStream(out);
            long[] total={0};
            for(String raw:paths){
                if(raw==null||raw.isBlank()||raw.startsWith("/")||raw.startsWith("\\")||Path.of(raw).normalize().startsWith("..")) throw new IllegalArgumentException("Unsafe cache path");
                Path target=root.resolve(raw).normalize();
                if(!target.startsWith(root)) throw new IllegalArgumentException("Unsafe cache path");
                if(!Files.exists(target)) continue;
                if(Files.isDirectory(target)){
                    try(var stream=Files.walk(target)){stream.filter(Files::isRegularFile).forEach(p->add(zip,root,p,total,limit));}
                } else add(zip,root,target,total,limit);
            }
            zip.finish();
        }
        static void add(java.util.zip.ZipOutputStream zip,Path root,Path file,long[] total,long limit){
            try{
                long size=Files.size(file); if(size>limit-total[0]) throw new IllegalArgumentException("Cache exceeds configured size limit");
                String name=root.relativize(file).toString().replace(java.io.File.separatorChar,'/');
                zip.putNextEntry(new java.util.zip.ZipEntry(name));
                try(InputStream in=Files.newInputStream(file)){in.transferTo(zip);}
                zip.closeEntry(); total[0]+=size;
            }catch(IOException e){throw new IllegalStateException("Unable to archive cache",e);}
        }
        static void extract(InputStream input,Path root,List<String> allowed) throws IOException {
            java.util.zip.ZipInputStream zip=new java.util.zip.ZipInputStream(input); java.util.zip.ZipEntry e;
            while((e=zip.getNextEntry())!=null){
                if(e.isDirectory()) continue;
                Path out=root.resolve(e.getName()).normalize();
                if(!out.startsWith(root)) throw new IllegalArgumentException("Cache archive contains unsafe path");
                boolean allowedPath=allowed.stream().anyMatch(p->e.getName().equals(p)||e.getName().startsWith(p.endsWith("/")?p:p+"/"));
                if(!allowedPath) continue;
                Files.createDirectories(out.getParent()); Files.copy(zip,out,StandardCopyOption.REPLACE_EXISTING); zip.closeEntry();
            }
        }
    }
}