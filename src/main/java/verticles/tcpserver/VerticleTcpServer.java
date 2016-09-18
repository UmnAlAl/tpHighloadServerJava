package verticles.tcpserver;

import http.HttpFileManager;
import http.HttpResponser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

import java.util.concurrent.*;

/**
 * Created by Installed on 07.09.2016.
 */
public class VerticleTcpServer extends AbstractVerticle {

    String host;
    int port;
    int cores;
    String documentRoot;

    NetServer server;
    HttpFileManager httpFileManager;
    ExecutorService executorService;
    CacheConfig cacheConfig;
    HttpResponser httpResponser;

    public VerticleTcpServer(String host, int port, int cores, String documentRoot, CacheConfig cacheConfig) {
        this.host = host;
        this.port = port;
        this.cores = cores;
        this.documentRoot = documentRoot;
        this.cacheConfig = cacheConfig;
    }

    @Override
    public void start() throws Exception {

        executorService = Executors.newWorkStealingPool(cores);
        //executorService = new ForkJoinPool(cores);
        httpFileManager = new HttpFileManager(executorService, cacheConfig, documentRoot);
        httpResponser = new HttpResponser(httpFileManager);

        server = vertx.createNetServer(
                new NetServerOptions()
                        .setPort(port)
                        .setHost(host)
                //        .setIdleTimeout(3)
                //        .setReceiveBufferSize(10 * 1024 * 1024)
                //        .setSendBufferSize(10 * 1024 * 1024)
                //        .setTcpNoDelay(true)
                        .setUsePooledBuffers(true)
        );

        server.connectHandler(netSocket -> {
            netSocket.handler(buffer -> {

                vertx.executeBlocking(future -> {
                            try {
                                Future<Buffer> futureResult = executorService.submit(() -> {
                                    return httpResponser.processRequest(buffer);
                                });
                                future.complete(futureResult.get());
                            }
                            catch (Exception ex) {
                                future.fail(ex.getCause());
                            }
                        },
                        ar -> {

                            Buffer outbuffer;

                            if(ar.succeeded()) {

                                outbuffer = (Buffer) ar.result();

                                if(netSocket.writeQueueFull()) {
                                    netSocket.pause();
                                    netSocket.drainHandler(done -> {
                                        netSocket.resume();
                                    });
                                }
                                netSocket.write(outbuffer).close();
                            }
                            else {
                                System.out.println("Failed: " + ar.cause());
                            }

                        });   //execute blocking
            }); //socket handler

        }) //connection handler
        .listen(res -> {
            if(res.succeeded()) {
                System.out.println("Server started at port " + server.actualPort());
            }
            else {
                System.out.println("Failed to bind: " + res.cause());
            }
        });

    }

}
