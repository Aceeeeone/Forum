package com.csu.be.forum.service;

import com.csu.be.forum.dao.DiscussPostMapper;
import com.csu.be.forum.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author nql
 * @version 1.0
 * @date 2021/1/20 21:56
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset ,limit);
    }

    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussRows(userId);
    }
}
