package com.forgeci.application.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileTime;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Phase 18 — repository-isolated job caches with size limits, TTL, and invalidation.
 * Namespace is the repository id so tenants cannot read each other's caches.
 */
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
        if (maxBytes <= 0 || ttlHours <= 0) {
            throw new IllegalArgumentException("Cache limits must be positive");
        }
    }

    public void restore(String namespace, String key, Path workspace, List<String> paths) {
        if (key == null || key.isBlank() || paths == null || paths.isEmpty()) {
            return;
        }
        Path archive = archive(namespace, key);
        try {
            if (!Files.isRegularFile(archive) || expired(archive)) {
                return;
            }
            try (InputStream in = new GZIPInputStream(Files.newInputStream(archive))) {
                CacheArchive.extract(in, workspace, paths);
            }
            Files.setLastModifiedTime(archive, FileTime.from(Instant.now()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to restore cache", e);
        }
    }

    public void save(String namespace, String key, Path workspace, List<String> paths) {
        if (key == null || key.isBlank() || paths == null || paths.isEmpty()) {
            return;
        }
        try {
            Files.createDirectories(root);
            Files.createDirectories(root.resolve(safe(namespace)).normalize());
            Path archive = archive(namespace, key);
            Path temp = Files.createTempFile(root, "forgeci-cache-", ".tmp");
            try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(temp))) {
                CacheArchive.write(out, workspace, paths, maxBytes);
            }
            Files.move(temp, archive, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save cache", e);
        }
    }

    /** Phase 18.3 — invalidate a single key within a repository namespace. */
    public boolean invalidate(String namespace, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        try {
            return Files.deleteIfExists(archive(namespace, key));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to invalidate cache", e);
        }
    }

    /** Invalidate every cache entry for a repository (tenant isolation cleanup). */
    public int invalidateNamespace(String namespace) {
        Path dir = root.resolve(safe(namespace)).normalize();
        if (!Files.isDirectory(dir)) {
            return 0;
        }
        int count = 0;
        try (var stream = Files.list(dir)) {
            for (Path p : stream.toList()) {
                if (Files.deleteIfExists(p)) {
                    count++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to invalidate cache namespace", e);
        }
        return count;
    }

    public String namespaceFor(UUID repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("repositoryId is required for cache namespace");
        }
        return repositoryId.toString();
    }

    private boolean expired(Path file) throws IOException {
        return Files.getLastModifiedTime(file).toInstant().plus(ttl).isBefore(Instant.now());
    }

    private Path archive(String namespace, String key) {
        String safeNamespace = safe(namespace);
        try {
            String digest = hex(MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            Path path = root.resolve(safeNamespace).resolve(digest + ".zip.gz").normalize();
            if (!path.startsWith(root)) {
                throw new IllegalArgumentException("Invalid cache path");
            }
            return path;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash cache key", e);
        }
    }

    /** Allow UUID-style namespaces (hyphens) and simple tokens. */
    private static String safe(String value) {
        if (value == null || !value.matches("[A-Za-z0-9._-]{1,120}")) {
            throw new IllegalArgumentException("Invalid cache namespace");
        }
        return value;
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            out.append(String.format("%02x", b));
        }
        return out.toString();
    }

    static final class CacheArchive {
        private CacheArchive() {}

        static void write(OutputStream output, Path workspace, List<String> paths, long limit)
                throws IOException {
            try (ZipOutputStream zip = new ZipOutputStream(output)) {
                long[] total = {0};
                for (String raw : paths) {
                    Path target = safeWorkspacePath(workspace, raw);
                    if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                        continue;
                    }
                    if (Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS)) {
                        try (var stream = Files.walk(target)) {
                            stream.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
                                    .forEach(p -> add(zip, workspace, p, total, limit));
                        }
                    } else {
                        add(zip, workspace, target, total, limit);
                    }
                }
            }
        }

        private static void add(
                ZipOutputStream zip, Path workspace, Path file, long[] total, long limit) {
            try {
                long size = Files.size(file);
                if (size > limit - total[0]) {
                    throw new IllegalArgumentException("Cache exceeds configured size limit");
                }
                String name = workspace.relativize(file).toString().replace(File.separatorChar, '/');
                zip.putNextEntry(new ZipEntry(name));
                try (InputStream in = Files.newInputStream(file)) {
                    in.transferTo(zip);
                }
                zip.closeEntry();
                total[0] += size;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to archive cache", e);
            }
        }

        static void extract(InputStream input, Path workspace, List<String> allowed)
                throws IOException {
            try (ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    Path output = workspace.resolve(entry.getName()).normalize();
                    if (!output.startsWith(workspace.toAbsolutePath().normalize())) {
                        throw new IllegalArgumentException("Cache archive contains unsafe path");
                    }
                    String entryName = entry.getName();
                    boolean allowedPath = allowed.stream()
                            .anyMatch(p -> entryName.equals(p)
                                    || entryName.startsWith(p.endsWith("/") ? p : p + "/"));
                    if (!allowedPath) {
                        continue;
                    }
                    Files.createDirectories(output.getParent());
                    Files.copy(zip, output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        private static Path safeWorkspacePath(Path workspace, String raw) {
            if (raw == null
                    || raw.isBlank()
                    || raw.startsWith("/")
                    || raw.startsWith("\\")
                    || Path.of(raw).normalize().startsWith("..")) {
                throw new IllegalArgumentException("Unsafe cache path");
            }
            Path root = workspace.toAbsolutePath().normalize();
            Path target = root.resolve(raw).normalize();
            if (!target.startsWith(root) || Files.isSymbolicLink(target)) {
                throw new IllegalArgumentException("Unsafe cache path");
            }
            return target;
        }
    }
}
