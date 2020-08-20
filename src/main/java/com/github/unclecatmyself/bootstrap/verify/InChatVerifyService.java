package com.github.unclecatmyself.bootstrap.verify;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Set;

/**
 * 用户校验层
 * Created by MySelf on 2018/11/22.
 */
public interface InChatVerifyService {

    Boolean verifyToken(String token);

    Set<String> getAllUserIdByGroupId(String groupId);

    boolean verifyGroup(JSONObject userId);
}