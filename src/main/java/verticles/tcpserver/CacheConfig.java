package verticles.tcpserver;

/**
 * Created by Installed on 11.09.2016.
 */
public interface CacheConfig {

    boolean isEnabled();
    long maximumWeight();
    long expireAfterAccessMinutes();
    long expireAfterWriteMinutes();
    long maxFileSize();

}
