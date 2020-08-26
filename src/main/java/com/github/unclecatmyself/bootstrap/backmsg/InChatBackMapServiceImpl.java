package com.github.unclecatmyself.bootstrap.backmsg;

import com.github.unclecatmyself.common.constant.Constans;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 列入项目中，默认返回实现
 * Created by MySelf on 2018/11/23.
 */
@Service
public class InChatBackMapServiceImpl implements InChatBackMapService {

    public Map<String, String> loginSuccess() {
        Map<String,String> backMap = new HashMap<String,String>();
        backMap.put(Constans.TYPE,Constans.LOGIN);
        backMap.put(Constans.SUCCESS,Constans.TRUE);
        return backMap;
    }


    public Map<String, String> loginError() {
        Map<String,String> backMap = new HashMap<String,String>();
        backMap.put(Constans.TYPE,Constans.LOGIN);
        backMap.put(Constans.SUCCESS,Constans.FALSE);
        return backMap;
    }


    public Map<String, String> sendMe(String value) {
        Map<String,String> backMap = new HashMap<String,String>();
        backMap.put(Constans.TYPE,Constans.SENDME);
        backMap.put(Constans.VALUE,value);
        return backMap;
    }


    public Map<String, String> sendBack(String otherOne, String value) {
        Map<String,String> backMap = new HashMap<String,String>();
        backMap.put(Constans.TYPE,Constans.SENDTO);
        backMap.put(Constans.VALUE,value);
        backMap.put(Constans.ONE,otherOne);
        return backMap;
    }


    public Map<String, String> getMsg(String token, String value) {
        Map<String, String> backMap = new HashMap<String, String>();
        backMap.put(Constans.TYPE, Constans.SENDTO);
        backMap.put(Constans.FROM, token);
        backMap.put(Constans.VALUE, value);
        return backMap;
    }


    public Map<String, Object> sendGroup(String userId, String value, String groupId, boolean b) {
        Map<String, Object> backMap = new HashMap<String, Object>();
        backMap.put(Constans.TYPE, Constans.SENDGROUP);
        backMap.put(Constans.FROM, userId);
        backMap.put(Constans.VALUE, value);
        backMap.put(Constans.GROUPID, groupId);
        backMap.put(Constans.Notify, b);
        return backMap;
    }

}
