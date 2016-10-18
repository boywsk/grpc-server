package com.gomeplus.grpc.utils;

import com.gomeplus.grpc.global.Global;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wangshikai on 2016/7/19.
 */
public class ZKClient {

    private static Logger LOG = LoggerFactory.getLogger(ZKClient.class);

    private static CuratorFramework CLIENT;

    private static String ROOT_PATH = Global.ZK_PATH;
    private static String IP_PORT = Global.ZK_IP_PORT;

    private static CountDownLatch LATCH = new CountDownLatch(1);

    private static ConcurrentSkipListSet<Boolean> CONTAINS_SET = new ConcurrentSkipListSet<>();

    public static ConcurrentHashMap<String,ManagedChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private ZKClient() {
    }

    private static ZKClient INSTANCE = new ZKClient();

    public static ZKClient getInstance() {
        return INSTANCE;
    }

    public CuratorFramework init(int port) {
        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString(IP_PORT)
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                        //.namespace(namespace)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000))
                .build();
        ConnectionStateListener listener = new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.CONNECTED) {
                    LOG.info("ZK连接成功");
                    LATCH.countDown();
                }
            }
        };
        CLIENT.getConnectionStateListenable().addListener(listener);
        CLIENT.start();
        try {
            LATCH.await();

            //创建根节点
            createRootNode(ROOT_PATH);

            //创建子节点
            String childPath = "";
            try {
                InetAddress address = InetAddress.getLocalHost();
                childPath = ROOT_PATH + "/" + address.getHostAddress() + ":" + port;

                createChildNode(childPath);

                LOG.info("创建临时子节点 childPath:{}", childPath);
            } catch (UnknownHostException e) {
                //e.printStackTrace();
                LOG.error("获取本机地址失败,检查......");
            }
        } catch (InterruptedException e) {
            LOG.error("error:{}", e);
            //e.printStackTrace();
        }

        return CLIENT;
    }

    public static void createRootNode(String rootPath) {
        try {
            Stat state = CLIENT.checkExists().forPath(rootPath);
            if (state == null) {
                CLIENT.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(rootPath);
            } else {
                LOG.info("根节点已经存在,ROOT_PATH:{}", rootPath);
            }
        } catch (Exception e) {
            LOG.error("error:{}", e);
            //e.printStackTrace();
        }
    }

    public static void createChildNode(final String path) {
        try {
            CLIENT.create().withMode(CreateMode.EPHEMERAL).forPath(path);
            CLIENT.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    try {
                        Stat nodeStat = CLIENT.checkExists().forPath(path);
                        if (nodeStat == null) {
                            //createChildNode(path);
                            CLIENT.create().withMode(CreateMode.EPHEMERAL).forPath(path);
                            LOG.info("ZK 连接状态变化,重新注册临时子节点......");
                        } else {
                            LOG.info("ZK 子节点已经存在:{}", path);
                            List<String> pathList = CLIENT.getChildren().forPath(ROOT_PATH);
                            for(String p : pathList){
                                LOG.info("子节点路径:{},子节点长度：{}",p,pathList.size());
                            }
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOG.error("error:{}", e);
                    }
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("error:{}", e);
        }
    }


    public static void getChildrenPath(final String rootPath) {
        try {
            final Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        LOG.info("-----------------------ZK子节点发生变化------------------");
                        try {
                            List<String> childrenPaths = CLIENT.getChildren().forPath(rootPath);
                            Iterator<Map.Entry<String,ManagedChannel>> it  = CHANNEL_MAP.entrySet().iterator();
                            while(it.hasNext()){
                                Map.Entry<String,ManagedChannel> e = it.next();
                                if(!childrenPaths.contains(e.getKey())){
                                    CHANNEL_MAP.get(e.getKey()).shutdownNow();
                                    it.remove();
                                }
                            }
                            for (String ipPort : childrenPaths) {
                                if(!CHANNEL_MAP.containsKey(ipPort)){
                                    String[] ip_port = ipPort.split(":");
                                    ManagedChannel channel = ManagedChannelBuilder.forAddress(ip_port[0], Integer.parseInt(ip_port[1])).usePlaintext(true).build();
                                    CHANNEL_MAP.put(ipPort,channel);
                                }
                                LOG.info("--------------------获取到的ZK子节点内容:" + ipPort + "\t 子节点长度:" + childrenPaths.size());
                            }
                            getChildrenPath(rootPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    LOG.info("监听变化-----------------------发生变化");
                }
            };

            List<String> childrenPaths = CLIENT.getChildren().usingWatcher(watcher).forPath(rootPath);
            for (String ipPort : childrenPaths) {
                if(!CHANNEL_MAP.containsKey(ipPort)){
                    String[] ip_port = ipPort.split(":");
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(ip_port[0], Integer.parseInt(ip_port[1])).usePlaintext(true).build();
                    CHANNEL_MAP.put(ipPort,channel);
                }
                LOG.info("--------------------获取到的ZK子节点内容:" + ipPort);
            }
            LOG.info("--------------------获取到的ZK子节点长度:" + childrenPaths.size());

            if(!CONTAINS_SET.contains(true)){
                CONTAINS_SET.add(true);
                CLIENT.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                    @Override
                    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                        try {
                            CLIENT.getChildren().usingWatcher(watcher).forPath(rootPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("error:{}", e);
        }
    }

    public void getZKPath(String ipPort,String zkPath){
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString(ipPort)
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000))
                .build();
        ConnectionStateListener listener = new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.CONNECTED) {
                    LOG.info("ZK连接成功");
                    countDownLatch.countDown();
                }
            }
        };
        CLIENT.getConnectionStateListenable().addListener(listener);
        CLIENT.start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getChildrenPath(zkPath);
    }

    public static void main(String[] args) {
        ZKClient.IP_PORT = "10.125.3.31:2181"; // 开发环境zk地址
        ZKClient.ROOT_PATH = "/im-dispatcher"; // zk 调度服务根节点

        CLIENT = CuratorFrameworkFactory
                .builder()
                .connectString(IP_PORT)
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000))
                .build();
        ConnectionStateListener listener = new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.CONNECTED) {
                    LOG.info("ZK连接成功");
                    LATCH.countDown();
                }
            }
        };
        CLIENT.getConnectionStateListenable().addListener(listener);
        CLIENT.start();
        try {
            LATCH.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getChildrenPath(ZKClient.ROOT_PATH);
    }

}
