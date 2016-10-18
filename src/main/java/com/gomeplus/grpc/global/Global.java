package com.gomeplus.grpc.global;


import com.gomeplus.grpc.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class Global {
    private static Logger LOG = LoggerFactory.getLogger(Global.class);

    public static String CONFIG_FILE = "config.properties";

    public static int SERVER_PORT = 8899;

    //zookeeper
    public static String ZK_IP_PORT;
    public static String ZK_PATH;

    //mongo dbName
    public static String MONGODB_DBNAME;

    static {
        Properties conf = PropertiesUtils.LoadProperties(CONFIG_FILE);
        //重新加载配置文件
        CONFIG_FILE = conf.getProperty("config-file");
        LOG.info("全局配置文件路径:{}",CONFIG_FILE);
        ZK_PATH = conf.getProperty("zookeeper-path");
        LOG.info("服务集群资源zk 根节点:{}",ZK_PATH);

        Properties properties = PropertiesUtils.LoadProperties(CONFIG_FILE);
        ZK_IP_PORT = properties.getProperty("zookeeperAddress");
        MONGODB_DBNAME = properties.getProperty("mongodb.dbName");
        SERVER_PORT = Integer.parseInt(properties.getProperty("server.port"));
    }

}
