package com.gomeplus.grpc.impl;

import com.gomeplus.grpc.model.Group;
import com.gomeplus.grpc.mongo.GroupDao;
import com.gomeplus.grpc.protobuf.GomeplusRpcServices;
import com.gomeplus.grpc.protobuf.SaveGroupServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangshikai on 2016/10/17.
 */
public class SaveGroupServiceImpl extends SaveGroupServiceGrpc.SaveGroupServiceImplBase{
    private static Logger LOG = LoggerFactory.getLogger(SaveGroupServiceImpl.class);
    private static GroupDao GROUP_DAO = new GroupDao();

    public void saveGroupService(com.gomeplus.grpc.protobuf.GomeplusRpcServices.RequestSaveGroup request,
                                 io.grpc.stub.StreamObserver<com.gomeplus.grpc.protobuf.GomeplusRpcServices.ResponseSaveGroup> responseObserver) {
        Group group=new Group();
        group.setGroupId(request.getGroup().getGroupId());
        group.setIsDele(request.getGroup().getIsDele());
        group.setIsAudit(request.getGroup().getIsAudit());
        group.setType(request.getGroup().getType());
        group.setUserId(request.getGroup().getUserId());
        long nowTime=System.currentTimeMillis();
        group.setCreateTime(nowTime);
        group.setUpdateTime(nowTime);
        boolean result = GROUP_DAO.save(request.getAppId(), group);
        responseObserver.onNext(GomeplusRpcServices.ResponseSaveGroup.newBuilder().setResult(result).build());
        responseObserver.onCompleted();
        LOG.info("SaveGroupServiceGrpc.saveGroupService success!");
    }
}
