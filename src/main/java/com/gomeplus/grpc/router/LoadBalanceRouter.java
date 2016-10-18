package com.gomeplus.grpc.router;

import com.gomeplus.grpc.global.Global;
import com.gomeplus.grpc.utils.ZKClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by wangshikai on 2016/10/14.
 */
public class LoadBalanceRouter {
    private static Logger LOG = LoggerFactory.getLogger(LoadBalanceRouter.class);

    private LoadBalanceRouter() {
    }

    public static LoadBalanceRouter INSTANCE = new LoadBalanceRouter();

    static {
        INSTANCE.init(Global.ZK_IP_PORT, Global.ZK_PATH);
    }

    public void init(final String zkIpPort, final String zkPath) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ZKClient.getInstance().getZKPath(zkIpPort, zkPath);
            }
        });
    }

    public ManagedChannel getRouterChannel() {
        long seed = System.nanoTime();
        Object[] arr = ZKClient.CHANNEL_MAP.entrySet().toArray();
        long hashValue = seed % arr.length;
        Map.Entry<String, ManagedChannel> entry = (Map.Entry<String, ManagedChannel>) arr[(int) hashValue];
        String ipPort = entry.getKey();
        ManagedChannel channel = ZKClient.CHANNEL_MAP.get(entry.getKey());
        if (channel.isTerminated()) {
            LOG.info("channel is terminated, create new channel!");
            channel = ManagedChannelBuilder.forAddress(ipPort.split(":")[0], Integer.parseInt(ipPort.split(":")[1])).usePlaintext(true).build();
        }
        return channel;
    }

}
