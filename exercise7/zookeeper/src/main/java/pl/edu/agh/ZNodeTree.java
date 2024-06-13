package pl.edu.agh;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;


public class ZNodeTree {
    private final ZooKeeper zk;

    public ZNodeTree(ZooKeeper zk) {
        this.zk = zk;
    }

    public String printTree(String znode) throws InterruptedException, KeeperException {
        if (zk.exists(znode, false) == null) {
            return "";
        }
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printTree(znode, znode, indent, sb);
        return sb.toString();
    }

    private void printTree(String znode, String nodeName, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(nodeName);
        sb.append("/\n");
        try {
            for (String child : zk.getChildren(znode, false)) {
                printTree(znode + "/" + child, child, indent + 1, sb);
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|   ");
        }
        return sb.toString();
    }
}
