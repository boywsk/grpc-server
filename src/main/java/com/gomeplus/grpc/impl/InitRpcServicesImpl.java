package com.gomeplus.grpc.impl;

import com.gomeplus.grpc.utils.LoadClassUtil;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 * 初始化绑定所有的rpc接口实现
 * Created by wangshikai on 2016/10/13.
 */
public class InitRpcServicesImpl {
    private static Logger LOG = LoggerFactory.getLogger(InitRpcServicesImpl.class);

    public static void init(ServerBuilder serverBuilder) {
        Package pack = InitRpcServicesImpl.class.getPackage();
        try {
            Set<Class<?>> clazzSet = LoadClassUtil.getClasses(pack.getName());
            for (Class clz : clazzSet) {
                Object clazz = clz.newInstance();
                if(clazz instanceof BindableService){
                    BindableService service = (BindableService) clazz;
                    //绑定实现
                    serverBuilder.addService(service);
                }
            }
        } catch (InstantiationException e) {
            LOG.error("error:{}", e);
        } catch (IllegalAccessException e) {
            LOG.error("error:{}", e);
        }
    }
}
