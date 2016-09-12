package http;

import io.vertx.core.buffer.Buffer;
import verticles.tcpserver.CacheConfig;

/**
 * Created by Installed on 12.09.2016.
 */
public class HttpResponser {

    HttpFileManager httpFileManager;

    public HttpResponser(int cores, CacheConfig cacheConfig, String documentRoot) {
        this.httpFileManager = new HttpFileManager(cores, cacheConfig, documentRoot);
    }

    public Buffer processRequest(Buffer input) {
        HttpRequestParser requestParser = new HttpRequestParser();
        requestParser.parseRequest(input);
    }


}
