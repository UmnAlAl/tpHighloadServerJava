package http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.*;
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

    private CacheConfig cacheConfig;
    private Cache<String, Buffer> fileCache;
    private Vertx vertx;
    private String documentRoot;

    public HttpFileManager(CacheConfig cacheConfig, String documentRoot) {
        this.cacheConfig = cacheConfig;
        this.vertx = SingleVertx.getInstance();
        this.documentRoot = documentRoot;

        this.fileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheConfig.expireAfterAccessMinutes(), TimeUnit.MINUTES)
                .expireAfterWrite(cacheConfig.expireAfterWriteMinutes(), TimeUnit.MINUTES)
                .maximumWeight(cacheConfig.maximumWeight())
                .weigher((String s, Buffer buf) -> {
                    return buf.length();
                })
                .recordStats()
                .softValues()
                //.weakKeys()
                //.weakValues()
                .build();
    }

    public Buffer readFile(String path) {

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

    private Buffer readFileDirectly(String path) {
        return vertx.fileSystem().readFileBlocking(path);
    }

    private Buffer readFileWithCache(String path) {

        Buffer fileFromCache = null;
        //System.out.println(fileCache.stats() + String.valueOf(fileCache.stats().hitRate()));
        if((fileFromCache = fileCache.getIfPresent(path)) == null) {

            try {
                Buffer readedFile = vertx.fileSystem().readFileBlocking(path);
                if(readedFile.length() <= cacheConfig.maxFileSize()) {
                    fileCache.put(path, readedFile);
                }
                return readedFile;
            }
            catch (Exception ex) {
                return null;
            }

        }
        else {
            return fileFromCache;
        }
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
