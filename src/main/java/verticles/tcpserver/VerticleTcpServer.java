package verticles.tcpserver;

import http.HttpFileManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

/**
 * Created by Installed on 07.09.2016.
 */
public class VerticleTcpServer extends AbstractVerticle {

    String host;
    int port;
    NetServer server;

    HttpFileManager httpFileManager;

    public VerticleTcpServer(String host, int port, int cores, CacheConfig cacheConfig) {
        this.host = host;
        this.port = port;
        this.httpFileManager = new HttpFileManager(cores, cacheConfig);
    }

    @Override
    public void start() throws Exception {

        server = vertx.createNetServer(
                new NetServerOptions()
                        .setPort(port)
                        .setHost(host)
                //        .setIdleTimeout(5)
                //        .setReceiveBufferSize(128 * 1024)
                //        .setSendBufferSize(128 * 1024)
                //        .setTcpNoDelay(true)
                        .setUsePooledBuffers(true)
        );


        server.connectHandler(netSocket -> {
            netSocket.handler(buffer -> {
                //String data = buffer.toString();
                //System.out.println("Data: [" + data + "] received from " + netSocket.remoteAddress());

                vertx.executeBlocking(future -> {
                            try {
                                java.util.concurrent.Future<Buffer> futureResult = httpFileManager.readFile(buffer);
                                future.complete(futureResult.get());
                            }
                            catch (Exception ex) {
                                future.fail(ex.getCause());
                            }
                        },
                        ar -> {

                            Buffer outbuffer = Buffer.buffer();

                            if(ar.succeeded()) {
                                if(ar.result() != null)
                                    outbuffer.appendBuffer((Buffer) ar.result());
                                else
                                    outbuffer.appendString("Failed to read!!!");

                                if(netSocket.writeQueueFull()) {
                                    netSocket.pause();
                                    netSocket.drainHandler(done -> {
                                        netSocket.resume();
                                    });
                                }
                                netSocket.write(outbuffer);

                            }
                            else {
                                System.out.println("Failed to read file: " + ar.cause());
                            }

                        });   //execute blocking
            }); //socet handler

        })
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
