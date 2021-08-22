package org.apache.zookeeper.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Jie Zhao
 * @date 2021/8/21 23:58
 */
@Slf4j
public class CreateSessionDemo implements Watcher {

    private static ZooKeeper zooKeeper;

    @Override
    public void process(WatchedEvent event) {
        // 当字节点列表发生改变时，服务器端会发送 NodeChildrenChanged通知，需要重新获取子节点列表。
        // 注意：通知是一次性的，需要反复注册监听
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            List<String> children = null;
            try {
                children = zooKeeper.getChildren("/lg-persistent", true);
                System.out.println("/lg-persistent节点的子节点：" + children);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        // 当连接创建了，服务器发送给客户端 SyncConnected 事件
        if (event.getState() == Event.KeeperState.SyncConnected) {
            try {
                // 同步创建节点
                // createNodeSync();

                // 获取节点数据
                getNodeData();

                // 获取当前节点的子节点
                getChildrenList();

                // 更新节点数据内容
                updateNodeSync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateNodeSync() throws InterruptedException, KeeperException {
        byte[] data = zooKeeper.getData("/lg-persistent", false, null);
        System.out.println("/lg-persistent节点数据内容：" + new String(data));
        Stat stat = zooKeeper.setData("/lg-persistent", "持久节点更新内容".getBytes(), -1);

        byte[] data2 = zooKeeper.getData("/lg-persistent", false, null);
        System.out.println("/lg-persistent更新后数据内容：" + new String(data2));
    }

    private void getChildrenList() throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren("/lg-persistent", true);
        System.out.println("/lg-persistent节点的子节点：" + children);
    }

    private void getNodeData() throws InterruptedException, KeeperException {
        byte[] data = zooKeeper.getData("/lg-persistent", false, null);
        System.out.println("/lg-persistent节点数据内容：" + new String(data));
    }

    private void createNodeSync() throws InterruptedException, KeeperException {
        // 持久节点
        String persistentNode = zooKeeper.create("/lg-persistent", "持久节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        // 临时节点
        String ephemeralNode = zooKeeper.create("/lg-ephemeral", "临时节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    public static void main(String[] args) throws Exception {

        /**
         * 客戶端可以通过创建一个ZooKeeper实例来连接 zk服务器
         * new ZooKeeper(String connectString, int sessionTimeout, Watcher watcher)
         * connectString：服务器地址：ip:port
         * sessionTimeout: 会话超时时间。单位毫秒
         * watcher：监听器（当特定事件触发监听时，zk会通过Watcher通知到客户端）
         */
        zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new CreateSessionDemo());
        System.out.println("zookeeper客戶端狀態：" + zooKeeper.getState());
        log.info("zookeeper客戶端狀態：{}", zooKeeper.getState());
        log.info("===========client connected to zookeeper=========");
        System.out.println("===========client connected to zookeeper=========");
        Thread.sleep(Integer.MAX_VALUE);
    }


}
