package http;

import io.vertx.core.buffer.Buffer;
import utils.HttpUtils;
import verticles.tcpserver.CacheConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Future;

/**
 * Created by Installed on 12.09.2016.
 */
public class HttpResponser {

    HttpFileManager httpFileManager;

    public HttpResponser(HttpFileManager httpFileManager) {
        this.httpFileManager = httpFileManager;
    }

    public Buffer processRequest(Buffer input) {

        Buffer output = Buffer.buffer().appendString("HTTP/");

        HttpRequestParser requestParser = new HttpRequestParser();
        String requestUri = null;
        Future<Buffer> futureFile = null;
        boolean isGet = false;
        long contentLength = 0;
        String contentType;
        try {
            requestParser.parseRequest(input);
            requestUri = requestParser.getUri();

            //check file
            if((contentLength = httpFileManager.checkFileExistsAndGetLength(requestUri)) == -1) {
                if(requestUri.endsWith("/")) {
                    throw new HttpException(403, HttpUtils.responsCodeMsgs.get(403));
                }
                throw new HttpException(404, HttpUtils.responsCodeMsgs.get(404));
            }

            //if method GET - start reading file
            if(isGet = requestParser.getMethod().equals("GET")) {
                futureFile = httpFileManager.readFile(requestUri);
            }

            //get content length
            //contentLength = httpFileManager.getFileLength(requestUri);

            //get content type
            contentType = HttpUtils.contentTypeMap.get(requestParser.getExtension());

            //success header
            output
                    .appendString(requestParser.getVersion())
                    .appendString(" ")
                    .appendString("200")
                    .appendString(" ")
                    .appendString(HttpUtils.responsCodeMsgs.get(200))
                    .appendString("\r\n");

            //add header
            addDate(output)
                    .addServer(output)
                    .addContentType(output, contentType)
                    .addContentLength(output, Long.toString(contentLength))
                    .addConnection(output, "keep-alive");

            //new line after header
            output.appendString("\r\n");

            //if get then append file
            if(isGet) {
                try {
                    output.appendBuffer(futureFile.get());
                }
                catch (Exception ex) {
                    //couldnt read the file
                    ex.printStackTrace();
                    throw new HttpException(500, HttpUtils.responsCodeMsgs.get(500));
                }
            }

        }
        catch (HttpException ex) {
            return processException(requestParser, ex.getCode(), ex.getMessage(), "Error. " + ex.getMessage());
        }
        catch (Exception ex) {
            return processException(requestParser, 400, HttpUtils.responsCodeMsgs.get(400), "Error. " + ex.getMessage());
        }

        return output;
    }

    public HttpResponser addDate(Buffer input) {
        input
                .appendString("Date: ")
                .appendString(getServerTime())
                .appendString("\r\n");
        return this;
    }

    public HttpResponser addServer(Buffer input) {
        input
                .appendString("Server: ")
                .appendString(HttpUtils.serverName)
                .appendString("\r\n");
        return this;
    }

    public HttpResponser addContentLength(Buffer input, String length) {
        input
                .appendString("Content-Length: ")
                .appendString(length)
                .appendString("\r\n");
        return this;
    }

    public HttpResponser addContentType(Buffer input, String type) {
        input
                .appendString("Content-type: ")
                .appendString(type)
                .appendString("\r\n");
        return this;
    }

    public HttpResponser addConnection(Buffer input, String connection) {
        input
                .appendString("Connection: ")
                .appendString(connection)
                .appendString("\r\n");
        return this;
    }

    public Buffer processException(HttpRequestParser requestParser,int code, String codeExplanation, String message) {
        Buffer output = Buffer.buffer().appendString("HTTP/");

        if(requestParser.getVersion() != null) {
            output.appendString(requestParser.getVersion());
        }
        else {
            output.appendString("1.1");
        }

        output
                .appendString(" ")
                .appendString(String.valueOf(code))
                .appendString(" ")
                .appendString(codeExplanation)
                .appendString("\r\n");

        addDate(output)
                .addServer(output)
                .addContentType(output, HttpUtils.contentTypeMap.get("html"))
                .addContentLength(output, Integer.toString(message.length()))
                .addConnection(output, "Closed");

        output.appendString("\r\n").appendString(message);

        return output;

    }

    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }


}
