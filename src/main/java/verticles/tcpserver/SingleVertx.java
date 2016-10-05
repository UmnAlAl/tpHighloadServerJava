package verticles.tcpserver;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Created by Installed on 14.09.2016.
 */
public class SingleVertx {

    public static class SingletonHolder {
        public static final Vertx instance = Vertx.vertx(new VertxOptions()
                .setEventLoopPoolSize(20)
                .setInternalBlockingPoolSize(20)
                .setWorkerPoolSize(5)
        );
    }

    public static Vertx getInstance() {
        return SingletonHolder.instance;
    }

}
