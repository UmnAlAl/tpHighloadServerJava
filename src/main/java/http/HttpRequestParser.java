package http;

import io.vertx.core.buffer.Buffer;
import utils.HttpUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

/**
 * Created by Installed on 11.09.2016.
 */
public class HttpRequestParser {

    private HashMap<String, String> requestParams = new HashMap<>();
    private String method;
    private String uri;
    private String version;
    private String extension;
    private boolean isDirectory;

    public static final String test = "GET /dir1/dir2/1.jpg?a=5 HTTP/1.1\r\nHost: localhost\r\nConnection: keep-alive\r\nCache-Control: max-age=0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\nUpgrade-Insecure-Requests: 1\r\n\r\n";

    public HttpRequestParser() {
        method = null;
        uri = null;
        version = null;
        extension = null;
        isDirectory = false;
    }

    public void parseRequest(Buffer requestBuffer) throws HttpException {
        String requestStr = requestBuffer.toString();
        int index;

        //get method
        index = requestStr.indexOf(" ");
        method = requestStr.substring(0, index);
        requestStr = requestStr.substring(index + 1);

        //check method
        if(!HttpUtils.supportedMethods.contains(method)) {
            throw new HttpException(400, HttpUtils.responsCodeMsgs.get(400));
        }

        //get uri
        index = requestStr.indexOf(" ");
        uri = requestStr.substring(0, index);
        requestStr = requestStr.substring(index + 1);

        //decode uri
        if(uri.contains("%")) {
            try {
                uri = URLDecoder.decode(uri, "UTF-8");
            }
            catch (UnsupportedEncodingException ex) {
                throw new HttpException(400, HttpUtils.responsCodeMsgs.get(400));
            }
        }

        //filter ..
        if(uri.contains("..")) {
            uri = uri.replace("..", "");
        }

        int qustChrInd = uri.lastIndexOf('?');
        if(qustChrInd != -1) {
            uri = uri.substring(0, qustChrInd);
        }

        //filter bad symbols
        uri = uri.replaceAll("[+^*#%:;&@<>'`|,]","");

        //get file extension and check if directory
        int lastSlash = uri.lastIndexOf('/');
        int lastPoint = uri.lastIndexOf('.');
        if(uri.endsWith("/")) {
            isDirectory = true;
            extension = HttpUtils.indexFileExtension;
        }
        else {
            isDirectory = false;

            //no extension
            if(lastSlash >= lastPoint) {
                throw new HttpException(400, HttpUtils.responsCodeMsgs.get(400));
            }
            extension = uri.substring(lastPoint + 1).toLowerCase();

            //unsupported extension
            if(!HttpUtils.supportedFormats.contains(extension)) {
                throw new HttpException(415, HttpUtils.responsCodeMsgs.get(415));
            }
        }

        //miss HTTP/
        requestStr = requestStr.substring(requestStr.indexOf("HTTP/") + 5);

        //get version
        index = requestStr.indexOf("\r\n");
        version = requestStr.substring(0, index);
        requestStr = requestStr.substring(index + 2);

        if(!HttpUtils.supportedHttpVersions.contains(version)) {
            throw new HttpException(505, HttpUtils.responsCodeMsgs.get(505));
        }

        //parse params
        String[] pairs = requestStr.split("\r\n");
        for (String pair:
             pairs) {
            if(pair.contains(": ")) {
                String[] splittedPair = pair.split(": ");
                requestParams.put(splittedPair[0], splittedPair[1]);
            }
        }

    }

    public String getUri() {return uri;}
    public String getVersion() {return version;}
    public String getMethod() {return method;}
    public String getExtension() {return extension;}
    public boolean isDirectory() {return isDirectory;}
}
