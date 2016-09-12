package http;

/**
 * Created by Installed on 12.09.2016.
 */
public class HttpException extends Exception {

    int code;

    public  HttpException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
