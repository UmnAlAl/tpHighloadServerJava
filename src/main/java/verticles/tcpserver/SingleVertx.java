package verticles.tcpserver;

import io.vertx.core.Vertx;

/**
 * Created by Installed on 14.09.2016.
 */
public class SingleVertx {

    public static class SingletonHolder {
        public static final Vertx instance = Vertx.vertx();
    }

    public static Vertx getInstance() {
        return SingletonHolder.instance;
    }

}
