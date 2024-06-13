package pl.edu.agh;

import org.apache.zookeeper.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NodeWatcher implements Watcher {
    private final ZooKeeper zk;
    private final String znode;
    private final String exec[];
    private Process process;
    private final ZNodeTree zNodeTree;


    public NodeWatcher(String hostPort, String znode, String[] exec) throws IOException, InterruptedException, KeeperException {
        this.znode = znode;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, 3000, this);
        zk.addWatch(znode, AddWatchMode.PERSISTENT_RECURSIVE);
        zNodeTree = new ZNodeTree(zk);
        awaitInput();
    }

    private void awaitInput() throws IOException, InterruptedException, KeeperException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String input = in.readLine();
            if (input.equalsIgnoreCase("quit")) {
                System.exit(0);
            } else if (input.equalsIgnoreCase("tree")) {
                System.out.println(zNodeTree.printTree(znode));
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            String path = watchedEvent.getPath();
            if (path.startsWith(znode)) {
                if (path.length() == znode.length()) {
                    try {
                        System.out.println("Starting process");
                        process = Runtime.getRuntime().exec(exec);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        int num = zk.getAllChildrenNumber(znode);
                        if (num == 1) {
                            System.out.println("Znode: " + znode + " has " + num + " child");
                        } else {
                            System.out.println("Znode: " + znode + " has " + num + " children");
                        }
                    } catch (KeeperException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            if(process != null) {
                System.out.println("Killing process");
                process.destroy();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                process = null;
            }
        }
    }
}
