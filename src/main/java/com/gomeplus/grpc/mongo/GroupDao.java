package com.gomeplus.grpc.mongo;


import com.gomeplus.grpc.model.Group;
import com.gomeplus.grpc.utils.BeanTransUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 群组数据库操作层
 */
public class GroupDao extends BaseDao {
	private final static Logger log = LoggerFactory.getLogger(GroupDao.class);
	private final static String collName = "t_group";

	/**
	 * 保存群组信息
	 * 
	 * @param group
	 */
	public boolean save(String appId,Group group) {
		String dbName = getDatabaseName(appId);
		Document doc = BeanTransUtils.bean2Document(group);
		return insert(dbName, collName, doc);
	}

//	/**
//	 * 修改群组信息
//	 *
//	 * @param group
//	 */
//	public void update(String appId,Group group) {
//		String dbName = getDatabaseName(appId);
//		MongoCollection<Document> coll = this.getCollection(dbName, collName);
//		Document doc = new Document();
//		String groupName  = group.getGroupName();
//
//		if(StringUtils.isNotBlank(groupName)) {
//			doc.put("groupName", groupName);
//		}
//		String groupDesc = group.getGroupDesc();
//		if(StringUtils.isNotBlank(groupDesc)) {
//			doc.put("groupDesc", groupDesc);
//		}
//		String avatar = group.getAvatar();
//		if(StringUtils.isNotBlank(avatar)) {
//			doc.put("avatar", avatar);
//		}
//		String qRcode = group.getqRcode();
//		if(StringUtils.isNotBlank(qRcode)) {
//			doc.put("qRcode", qRcode);
//		}
//		int capacity = group.getCapacity();
//		if(capacity > 0) {
//			doc.put("capacity", capacity);
//		}
//		int isAudit = group.getIsAudit();
//		if(isAudit >= 0) {
//			doc.put("isAudit", isAudit);
//		}
//		int isDele = group.getIsDele();
//		if(isDele >= 0) {
//			doc.put("isDele", isDele);
//		}
//		long userId = group.getUserId();
//		if (userId>=0) {
//			doc.put("userId", userId);
//		}
//		String subject = group.getSubject();
//		if (StringUtils.isNotBlank(subject)) {
//			doc.put("subject", subject);
//		}
//
//		doc.put("updateTime", System.currentTimeMillis());
//		Bson filter =Filters.eq("groupId", group.getGroupId());
//		try {
//			coll.updateOne(filter, new Document("$set", doc));
//			log.info("update group success!");
//		} catch (Exception e) {
//			log.error("update group fail exception",e);
//		}
//	}
//
//	/**
//	 * 修改群组修改时间
//	 * @param groupId
//	 */
//	@Deprecated
//	public void updateGroupTime(String appId,String groupId) {
//		String dbName = getDatabaseName(appId);
//		MongoCollection<Document> coll = this.getCollection(dbName, collName);
//		Document doc = new Document();
//		doc.put("updateTime", System.currentTimeMillis());
//		Bson filter = Filters.eq("groupId", groupId);
//		try {
//			coll.updateOne(filter, new Document("$set", doc));
//			log.info("update group time success");
//		} catch (Exception e) {
//			log.error("update group time fail exception",e);
//		}
//	}
//
//	/**
//	 * 根据groupId 获得group数据
//	 * @param groupId
//	 *
//	 */
//	public Group getGroup(String appId, String groupId) {
//		String dbName = getDatabaseName(appId);
//		MongoCursor<Document> cursor = null;
//		Group group = null;
//		try {
//			BasicDBObject filter = new BasicDBObject();
//			filter.put("groupId", groupId);
//			cursor = find(dbName, collName, filter);
//			if (cursor.hasNext()) {
//				Document item = cursor.next();
//				group = (Group) BeanTransUtils.document2Bean(item, Group.class);
//			}
//			log.info("GroupDao getGroupById success!");
//		} catch (Exception e) {
//			log.error("GroupDao getGroupById fail exception:", e);
//		} finally {
//			cursorClose(cursor);
//		}
//		return group;
//	}
//
//	/**
//	 * 设置群组的删除状态
//	 *
//	 * @param groupId
//	 *            群组id
//	 * @param isDel
//	 *            群主删除状态
//	 * @return boolean 是否修改成功
//	 */
//	public boolean setGroupIsDel(String appId,String groupId, int isDel) {
//		String dbName = getDatabaseName(appId);
//		BasicDBObject filter = new BasicDBObject();
//		filter.put("groupId", groupId);
//
//		Document newdoc = new Document();
//		newdoc.put("isDele", isDel);
//		newdoc.put("updateTime", System.currentTimeMillis());
//
//		return update(dbName, collName, filter, newdoc);
//	}
//
//	/**
//	 * 根据群组id列表和时间获取群组
//	 * @param groupIds
//	 * @param time
//	 * @return
//	 */
//	public List<Group> listGroup(List<String> groupIds, long time) {
//		List<Group> list = new ArrayList<Group>();
//		BasicDBObject where = new BasicDBObject();
//		where.put("groupId", new BasicDBObject(QueryOperators.IN, groupIds));
//		where.put("updateTime", new BasicDBObject(QueryOperators.GTE, time));
//		where.put("isDele", Constant.GROUP_DEL.E_GROUP_DEL_NOT.value);
//		MongoCursor<Document> cursor = null;
//		try {
//			cursor = find(dbName, collName, where);
//			while (cursor.hasNext()) {
//				Document item = cursor.next();
//				Group group = (Group) BeanTransUtils.document2Bean(item, Group.class);
//				list.add(group);
//			}
//			log.info("listGroup success!");
//		} catch (Exception e) {
//			log.error("listGroup fail exception",e);
//		}finally{
//			cursorClose(cursor);
//		}
//
//		return list;
//	}
//
//	/**
//	 * 保存对话的group信息
//	 * @param group
//	 */
//	@Deprecated
//	public void saveOrUpdateGroupById(String appId,String groupId,Group group) {
//		try{
//			String dbName = getDatabaseName(appId);
//			MongoCollection<Document> coll = this.getCollection(dbName, collName);
//			Document doc = new Document();
//			if(group.getType() > 0){
//				doc.put("type", group.getType());
//			}
//			if(group.getSeq() >= 0){
//				doc.put("seq",group.getSeq());
//			}
//			if(group.getIsDele() >= 0){
//				doc.put("isDele",group.getIsDele());
//			}
//			if(group.getCreateTime() > 0){
//				doc.put("createTime",group.getCreateTime());
//			}
//			if(group.getUpdateTime() > 0){
//				doc.put("updateTime",group.getUpdateTime());
//			}
//			if(StringUtils.isNotEmpty(group.getGroupName())){
//				doc.put("groupName",group.getGroupName());
//			}
//			Bson filter = Filters.eq("groupId", groupId);
//			coll.findOneAndUpdate(filter, new Document("$set", doc), new FindOneAndUpdateOptions().upsert(true));
//			log.info("GroupDao saveOrUpdateGroupId success!");
//		}catch (Exception e){
//			log.error("saveOrUpdateGroupId error:{},groupId:{}", e, group.getGroupId());
//		}
//	}
//
//	/**
//	 * 根据groupId获取group
//	 * @param appId
//	 * @param groupId
//	 * @return
//	 */
//	public Group getGroupById(int appId, String groupId) {
//		log.info("[getGroupById] appId=[{}],groupId=[{}]", appId, groupId);
//		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
//		Bson where = Filters.eq("groupId", groupId);
//		Group group=null;
//		try {
//			Document doc = coll.find(where).first();
//			group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
//			if(group != null) {
//				JedisClusterClient util = JedisClusterClient.INSTANCE;
//				String key = appId + "_groupSeqId" + "_" + groupId;
//				long seqId = 0L;
//				String value = util.getJedisCluster().get(key);
//				if(value != null) {
//					seqId = Long.valueOf(value);
//				}
//				group.setSeq(seqId);
//			}
//			log.info("getGroupById success!");
//		} catch (Exception e) {
//			log.error("getGroupById fail exception!",e);
//		}
//
//		return group;
//	}
//
//	/**
//	 * 获取用户群组列表
//	 *
//	 * @param appId
//	 * @param uid
//	 * @param time
//	 * @return
//	 */
//	public UserData listGroupByUid(int appId, long uid, long time, byte clientId) {
//		try {
//			log.info("[listGroupByUid] appId=[{}],uid=[{}],time=[{}]", appId, uid, time);
//			UserData.Builder pbGroupList = UserData.newBuilder();
//			GroupDao dao = new GroupDao();
//			GroupMemberDao memberDao = new GroupMemberDao();
//			MsgDao msgDao = new MsgDao();
//			Map<String, String> maxSeqMap = memberDao.listMemberMaxSeq(appId, uid);
//			List<GroupMember> members = memberDao.listMemberSeq(appId, uid, clientId);
//			if (CollectionUtils.isEmpty(members)) {
//				log.info("listGroupByUid members is empty!");
//				return pbGroupList.build();
//			}
//			log.info("[listGroupByUid] appId=[{}],uid=[{}],members size=[{}]", appId, uid, members.size());
//			for (GroupMember member : members) {
//				String groupId = member.getGroupId();
//				long receiveSeqId = member.getReceiveSeqId();
//				long maxSeq = 0;
//				String maxSeqValue = maxSeqMap.get(groupId);
//				if (!Strings.isNullOrEmpty(maxSeqValue)) {
//					maxSeq = Long.valueOf(maxSeqValue);
//				}
//				if (maxSeq > receiveSeqId) {
//					Group group = dao.getGroupById(appId, groupId);
//					if (group == null) {
//						continue;
//					}
//					group.setSeq(maxSeq);
//					long diff = maxSeq - receiveSeqId;
//					int size = 0;
//					if (diff >= 20L) {
//						size = 20;
//					} else {
//						size = (int) diff;
//					}
//					List<GroupMsg> msgs = msgDao.listGroupMsg(appId, uid, groupId, maxSeq, size,time);
//					group.setMsgs(msgs);
//					ImGroupMsg pbGroup = PbGroupMsgTools.grou2PbGroup(group, member);
//					if (pbGroup == null) {
//						continue;
//					}
//					pbGroupList.addGroup(pbGroup);
//				}
//			}
//			Map<String, Object> extraMap=new HashMap<String, Object>();
//			extraMap.put("time", System.currentTimeMillis());
//			pbGroupList.setExtra(JSON.toJSONString(extraMap));
//			log.info("listGroupByUid success!");
//			return pbGroupList.build();
//		} catch (Exception e) {
//			log.error("listGroupByUid fail exception ",e);
//			return null;
//		}
//	}


}
