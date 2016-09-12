package Main;

import http.HttpRequestParser;
import io.vertx.core.buffer.Buffer;

/**
 * Created by Installed on 11.09.2016.
 */
public class Main2 {

    public static void main(String[] args) {
        HttpRequestParser httpRequestParser = new HttpRequestParser();
        try {
            httpRequestParser.parseRequest(Buffer.buffer().appendString(HttpRequestParser.test));
        }
        catch (Exception ignore) {

        }
    }

}
