package utils;

import java.util.*;

/**
 * Created by Installed on 12.09.2016.
 */
public class HttpUtils {

    public static final String indexFileName = "index.html";
    public static final String indexFileExtension = "html";

    public static final String serverName = "JavaServer";

    public static final Map<Integer, String> responsCodeMsgs;
    static {
        responsCodeMsgs = new HashMap<>();
        responsCodeMsgs.put(200, "OK");
        responsCodeMsgs.put(400, "Bad Request");
        responsCodeMsgs.put(403, "Forbidden");
        responsCodeMsgs.put(404, "Not Found");
        responsCodeMsgs.put(405, "Method Not Allowed");
        responsCodeMsgs.put(415, "Unsupported Media Type");
        responsCodeMsgs.put(500, "Internal Server Error");
        responsCodeMsgs.put(501, "Not Implemented");
        responsCodeMsgs.put(505, "HTTP Version Not Supported");
    }

    public static final List<String> supportedMethods;
    static {
        supportedMethods = new LinkedList<>();
        supportedMethods.add("GET");
        supportedMethods.add("HEAD");
    }

    public static final List<String> supportedHttpVersions;
    static {
        supportedHttpVersions = new LinkedList<>();
        supportedHttpVersions.add("1.0");
        supportedHttpVersions.add("1.1");
    }

    public static final Map<String, String> contentTypeMap;
    public static final Set<String> supportedFormats;
    static {
        contentTypeMap = new HashMap<>();
        contentTypeMap.put("txt", "text/plain");
        contentTypeMap.put("html", "text/html");
        contentTypeMap.put("css", "text/css");
        contentTypeMap.put("js", "application/javascript");
        contentTypeMap.put("jpg", "image/jpeg");
        contentTypeMap.put("jpeg", "image/jpeg");
        contentTypeMap.put("png", "image/png");
        contentTypeMap.put("gif", "image/gif");
        contentTypeMap.put("swf", "application/x-shockwave-flash");
        supportedFormats = contentTypeMap.keySet();
    }

}
