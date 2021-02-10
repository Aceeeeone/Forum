package com.csu.be.forum.service;

import com.csu.be.forum.dao.UserMapper;
import com.csu.be.forum.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author nql
 * @version 1.0
 * @date 2021/1/31 23:15
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

}
