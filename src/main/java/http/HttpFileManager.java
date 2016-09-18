package http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import utils.HttpUtils;
import verticles.tcpserver.CacheConfig;
import verticles.tcpserver.SingleVertx;

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

    public HttpFileManager(ExecutorService executorService, CacheConfig cacheConfig, String documentRoot) {
        this.executorService = executorService;
        this.cacheConfig = cacheConfig;
        this.vertx = SingleVertx.getInstance();
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
            path += HttpUtils.indexFileName;
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
            //System.out.println(fileCache.stats().toString());

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

    public long checkFileExistsAndGetLength(String path) {
        String realPath = documentRoot + path;
        if(path.endsWith("/")) {
            realPath += HttpUtils.indexFileName;
        }
        File file = new File(realPath);
        if(file.exists())
            return file.length();
        else
            return -1;
    }

    public boolean checkFileExists(String path) {
        String realPath = documentRoot + path;
        if(path.endsWith("/")) {
            realPath += HttpUtils.indexFileName;
        }
        File file = new File(realPath);
        if(file.exists())
            return true;
        else
            return false;
    }

    public long getFileLength(String path) {
        String realPath = documentRoot + path;
        if(path.endsWith("/")) {
            realPath += HttpUtils.indexFileName;
        }
        File file = new File(realPath);
        return file.length();
    }

}
