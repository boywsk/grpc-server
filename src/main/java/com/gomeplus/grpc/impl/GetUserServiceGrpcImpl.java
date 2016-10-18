package com.gomeplus.grpc.impl;

import com.gomeplus.grpc.model.UserInfo;
import com.gomeplus.grpc.mongo.UserInfoDao;
import com.gomeplus.grpc.protobuf.GetUserServiceGrpc;
import com.gomeplus.grpc.protobuf.GomeplusRpcServices;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangshikai on 2016/10/8.
 */
public class GetUserServiceGrpcImpl extends GetUserServiceGrpc.GetUserServiceImplBase {
    private static Logger LOG = LoggerFactory.getLogger(GetUserServiceGrpcImpl.class);
    private static UserInfoDao USER_DAO = new UserInfoDao();
    @Override
    public void getUser(GomeplusRpcServices.RequestUser request, StreamObserver<GomeplusRpcServices.ResponseUser> responseObserver) {
        String appId = request.getAppId();
        long imUserId = request.getImUserId();

        long a = System.currentTimeMillis();
        UserInfo userInfo = USER_DAO.getUserInfoByImUserId(appId, imUserId);
        long b = System.currentTimeMillis();
        if(userInfo != null){
            GomeplusRpcServices.ResponseUser responseUser = GomeplusRpcServices.ResponseUser.newBuilder().setUid(userInfo.getUid())
                    .setToken(userInfo.getToken()).setTokenExpires(userInfo.getTokenExpires()).build();
            responseObserver.onNext(responseUser);
        }else{
            responseObserver.onNext(GomeplusRpcServices.ResponseUser.getDefaultInstance());
        }
        responseObserver.onCompleted();
        LOG.info("GRPC ->GetUserServiceGrpc.getUser success,appId:{},imUserId:{},passTime:{}",appId,imUserId,(b-a));
    }
}
