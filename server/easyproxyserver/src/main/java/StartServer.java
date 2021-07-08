import control.HttpServerInitializer;
import core.Keepalive;
import core.NettyServer;
import core.ServerInitializer;
import org.apache.commons.cli.*;

/**
 * @author yujian
 */
public class StartServer {
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", false, "Help");
        options.addOption("port", true, "server port");
        options.addOption("web", true, "web port");
        options.addOption("username", true, "web auth username");
        options.addOption("password", true, "web auth password");
        options.addOption("sync", false, "open sync, the response time can be controlled");
        options.addOption("time", false, "waiting for response time (SECONDS)");

        CommandLineParser parser = new DefaultParser();
        CommandLine       cmd    = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("options", options);
        } else {
            int port = Integer.parseInt(cmd.getOptionValue("port", "9675"));
            int time = Integer.parseInt(cmd.getOptionValue("time", "3"));
            int webPort = Integer.parseInt(cmd.getOptionValue("web", "18800"));
            String username = cmd.getOptionValue("username", "root");
            String password = cmd.getOptionValue("password", "123456");
            boolean sync = cmd.hasOption("sync");
            NettyServer nettyServer = new NettyServer();
            nettyServer.start(new ServerInitializer(time,sync),port);
            System.out.println("server started "+(sync?"sync":"")+" on port " + port);
            new Keepalive().ping();
            System.out.println("keepalive started");
            NettyServer webNettyServer = new NettyServer();
            webNettyServer.start(new HttpServerInitializer(username+":"+password),webPort);
            System.out.println("http server started  on port " + webPort);
        }
        // NettyServer nettyServer = new NettyServer();
        // nettyServer.start(new ServerInitializer(3,false),18888);
        // new KeepAlive().ping();
        //     System.out.println("keepalive started");
    }
}
