package org.apache.zookeeper.demo.util;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;

/**
 * @author Jie Zhao
 * @date 2021/8/22 14:26
 */
public class ZookeeperUtils {

    private static ZooKeeper zooKeeperClient;

    private ZookeeperUtils() {}

    public static void initClient(String connectString, int sessionTimeout, Watcher watcher) throws IOException {
        zooKeeperClient = new ZooKeeper(connectString, sessionTimeout, watcher);
    }

    public static String createNode(String path, byte data[], List<ACL> acl,
                                  CreateMode createMode) throws InterruptedException, KeeperException {
        return zooKeeperClient.create(path, data, acl, createMode);
    }
}
