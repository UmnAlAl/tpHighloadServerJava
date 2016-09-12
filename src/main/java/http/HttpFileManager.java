package http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import utils.HttpUtils;
import verticles.tcpserver.CacheConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.*;

/**
 * Created by Installed on 11.09.2016.
 */
public class HttpFileManager {

    ExecutorService executorService;
    CacheConfig cacheConfig;
    Cache<String, ByteArrayOutputStream> fileCache;
    Vertx vertx;
    String documentRoot;

    public HttpFileManager(int cores, CacheConfig cacheConfig, String documentRoot) {
        this.executorService = new ForkJoinPool(cores);
        this.cacheConfig = cacheConfig;
        this.vertx = Vertx.vertx();
        this.documentRoot = documentRoot;

        this.fileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheConfig.expireAfterAccessMinutes(), TimeUnit.MINUTES)
                .expireAfterWrite(cacheConfig.expireAfterWriteMinutes(), TimeUnit.MINUTES)
                .maximumWeight(cacheConfig.maximumWeight())
                .weigher((String s, ByteArrayOutputStream buf) -> {
                    return buf.size();
                })
                .recordStats()
                .build();
    }

    public Future<Buffer> readFile(String path) {

        if(path.endsWith("/")) {
            path += HttpUtils.indexFile;
        }
        path = this.documentRoot + path;

        if(cacheConfig.isEnabled()) {
            return readFileWithCache(path);
        }
        else {
            return readFileDirectly(path);
        }
    }

    private Future<Buffer> readFileDirectly(String path) {
        Future<Buffer> resFuture = executorService.submit(() -> {
            return vertx.fileSystem().readFileBlocking(path);
        });
        return resFuture;
    }

    private Future<Buffer> readFileWithCache(String path) {

        Future<Buffer> resFuture = executorService.submit(() -> {
            System.out.println(fileCache.stats().toString());

            ByteArrayOutputStream fileFromCache = new ByteArrayOutputStream();
            if((fileFromCache = fileCache.getIfPresent(path)) == null) {

                try {
                    Buffer readedFile = vertx.fileSystem().readFileBlocking(path);
                    if(readedFile.length() <= cacheConfig.maxFileSize()) {
                        ByteArrayOutputStream streamToCache = new ByteArrayOutputStream(readedFile.length());
                        streamToCache.write(readedFile.getBytes());
                        fileCache.put(path, streamToCache);
                    }
                    return readedFile;
                }
                catch (Exception ex) {
                    return null;
                }

            }
            else {
                return Buffer.buffer().appendBytes(fileFromCache.toByteArray());
            }

        });
        return resFuture;
    }

    public boolean checkFileExists(String path) {
        File file = new File(documentRoot + path);
        if(file.exists() && !file.isDirectory())
            return true;
        else
            return false;
    }

    public long getFileLength(String path) {
        File file = new File(documentRoot + path);
        return file.length();
    }

}
