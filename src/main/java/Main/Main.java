package Main;

import io.vertx.core.Vertx;
import org.apache.commons.cli.*;
import verticles.tcpserver.CacheConfig;
import verticles.tcpserver.SingleVertx;
import verticles.tcpserver.VerticleTcpServer;

/**
 * Created by Installed on 07.09.2016.
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = SingleVertx.getInstance();

        //parse arguments
        Options options = getCmdOptions();
        CommandLineParser parser = new BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException ex) {
            System.out.println(ex.getMessage());
            formatter.printHelp("java -jar ./target/HighloadServer-<version>-jar-with-dependencies.jar", options);
            return;
        }

        String host = cmd.getOptionValue("h");
        int port = Integer.parseInt(cmd.getOptionValue("p"));
        String docRoot = cmd.getOptionValue("r");
        int cores = Integer.parseInt(cmd.getOptionValue("c"));

        CacheConfig config = Main.getCacheConfig(cmd);

        // D:\tcpTest\http-test-suite-master
        // C:\Users\Installed\IdeaProjects\HighloadServer

        vertx.deployVerticle(new VerticleTcpServer(host, port, cores, docRoot, config), ar -> {
            System.out.println("VerticleTcpServer deployed");
        });
    }

    private static CacheConfig getCacheConfig(CommandLine cmd) {
        return new CacheConfig() {
            @Override
            public boolean isEnabled() {
                return cmd.hasOption("cen");
            }

            @Override
            public long maximumWeight() {
                return Long.parseLong(cmd.getOptionValue("cmw", "52428800"));
            }

            @Override
            public long expireAfterAccessMinutes() {
                return Long.parseLong(cmd.getOptionValue("ceaam", "52428800"));
            }

            @Override
            public long expireAfterWriteMinutes() {
                return Long.parseLong(cmd.getOptionValue("ceawm", "52428800"));
            }

            @Override
            public long maxFileSize() {
                return Long.parseLong(cmd.getOptionValue("ccmfsz", "524288"));
            }

        };

    }

    private static Options getCmdOptions() {
        /*
        * -h localhost
        * -p 80
        * -r C:\Users\Installed\IdeaProjects\HighloadServer
        * -c 4
        * -cen
        * -cmw 52428800
        * -ceaam 30
        * -caawm 30
        * -cmfsz 1048576
        * */

        Options options = new Options();

        Option host = new Option("h", "host", true, "host");
        host.setRequired(true);
        options.addOption(host);

        Option port = new Option("p", "port", true, "port");
        port.setRequired(true);
        options.addOption(port);

        Option docRoot = new Option("r", "root", true, "document root");
        docRoot.setRequired(true);
        options.addOption(docRoot);

        Option cores = new Option("c", "cores", true, "num of cores");
        cores.setRequired(true);
        options.addOption(cores);

        Option cacheEnabled = new Option("cen", "cache_enabled", false, "is guava cache enabled");
        cacheEnabled.setRequired(true);
        options.addOption(cacheEnabled);

        Option cacheMaxWeight = new Option("cmw", "cache_max_weight", true, "cache size");
        cacheMaxWeight.setRequired(false);
        options.addOption(cacheMaxWeight);

        Option cacheEAAM = new Option("ceaam", "cache_exp_acc_min", true, "cache expires after access in minutes");
        cacheEAAM.setRequired(false);
        options.addOption(cacheEAAM);

        Option cacheEAWM = new Option("ceawm", "cache_exp_wrt_min", true, "cache expires after write in minutes");
        cacheEAWM.setRequired(false);
        options.addOption(cacheEAWM);

        Option cacheMFSZ = new Option("cmfsz", "cache_max_file_size", true, "cache max file size");
        cacheMFSZ.setRequired(false);
        options.addOption(cacheMFSZ);

        return options;
    }

}
