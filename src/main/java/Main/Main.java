package Main;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import verticles.tcpserver.CacheConfig;
import verticles.tcpserver.SingleVertx;
import verticles.tcpserver.VerticleTcpServer;

/**
 * Created by Installed on 07.09.2016.
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = SingleVertx.getInstance();

        CacheConfig config = Main.getCache();

        // D:\tcpTest\http-test-suite-master
        // C:\Users\Installed\IdeaProjects\HighloadServer

        vertx.deployVerticle(new VerticleTcpServer("localhost", 80, 4, "D:\\tcpTest\\http-test-suite-master", config), ar -> {
            System.out.println("VerticleTcpServer deployed");
        });
    }

    public static CacheConfig getCache() {
        return new CacheConfig() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public long maximumWeight() {
                return 1024 * 1024 * 40;
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
                return 1024 * 512;
            }

        };

    }

}
