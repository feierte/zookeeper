package org.apache.zookeeper.demo;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * @author Jie Zhao
 * @date 2021/8/16 20:17
 */
public class ZookeeperDemo {

    private ZooKeeper zooKeeper;

    public ZookeeperDemo() {
        try {
            zooKeeper= new ZooKeeper("localhost:2181",
                    5000,
                    null, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(String path,String data){
        try {
            String newPath = zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ZookeeperDemo zookeeperDemo=new ZookeeperDemo();
        zookeeperDemo.add("/demo","2021");
    }
}
