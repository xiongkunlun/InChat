package com.github.unclecatmyself.common.base;

public interface UserService {

    String getById(String userId);

    void saveUser(String userId, String userInfo);

}
