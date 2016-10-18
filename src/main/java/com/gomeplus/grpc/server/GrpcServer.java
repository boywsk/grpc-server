package com.gomeplus.grpc.server;

import com.gomeplus.grpc.impl.InitRpcServicesImpl;
import com.gomeplus.grpc.utils.ZKClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Created by wangshikai on 2016/10/8.
 */
public class GrpcServer {
    private static Logger LOG = LoggerFactory.getLogger(GrpcServer.class);

    public static void main(String[] args) {
        try {
            //启动服务
            int serverPort = 8899;
            //可以接受端口参数
            if (args.length >= 1 && args[0] != null) {
                try {
                    serverPort = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    LOG.error("error:{}", e);
                }
            }

            ServerBuilder serverBuilder = ServerBuilder.forPort(serverPort);
            InitRpcServicesImpl.init(serverBuilder);

            final Server server = serverBuilder.build();
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdown();
                }
            }));
            LOG.info("GomeplusRpcServer started!");

            // 注册 ZK
            final int finalServerPort = serverPort;
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    //初始化ZK,并将服务地址发布到ZK根节点 "/gomeplus-grpc" 的子节点
                    ZKClient.getInstance().init(finalServerPort);
                }
            });

            server.awaitTermination();
        } catch (IOException e) {
            LOG.error("error:{}", e);
        } catch (InterruptedException e) {
            LOG.error("error:{}", e);
        }
    }

}
