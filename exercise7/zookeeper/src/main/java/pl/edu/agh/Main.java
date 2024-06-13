package pl.edu.agh;

public class Main {
    private static final String HOST_PORT = "127.0.0.1:2181";
    private static final String ZNODE = "/a";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Provide program and optional arguments");
            System.exit(1);
        }
        String exec[] = new String[args.length];
        System.arraycopy(args, 0, exec, 0, args.length);
        try {
            new NodeWatcher(HOST_PORT, ZNODE, exec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}