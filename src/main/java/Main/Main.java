package Main;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import verticles.tcpserver.CacheConfig;
import verticles.tcpserver.VerticleTcpServer;

/**
 * Created by Installed on 07.09.2016.
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        CacheConfig config = Main.getCache();

        vertx.deployVerticle(new VerticleTcpServer("localhost", 9095, 10, config), ar -> {
            System.out.println("VerticleTcpServer deployed");
        });
    }

    public static CacheConfig getCache() {
        return new CacheConfig() {
            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public long maximumWeight() {
                return 1024 * 1024;
            }

            @Override
            public long expireAfterAccessMinutes() {
                return 20;
            }

            @Override
            public long expireAfterWriteMinutes() {
                return 20;
            }

            @Override
            public long maxFileSize() {
                return 1024;
            }

        };

    }

}
