package com.csu.be.forum;

import com.csu.be.forum.dao.DiscussPostMapper;
import com.csu.be.forum.entity.User;
import com.csu.be.forum.service.FollowService;
import com.csu.be.forum.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.SpringVersion;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author nql
 * @version 1.0
 * @date 2021/2/26 2:10
 */
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class Tests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private FollowService followService;

    @Test
    public void test(){
        String version = SpringBootVersion.getVersion();
        String version1 = SpringVersion.getVersion();
        System.out.println(version);
        System.out.println(version1);
    }

    @Test
    public void filterTest(){
        String s = sensitiveFilter.filter("我经常去&赌&&博&");
        System.out.println(s);
    }

    @Test
    public void followeeTest(){
        long followeeCount = followService.findFolloweeCount(154, 3);
        System.out.println(followeeCount);
    }

}
