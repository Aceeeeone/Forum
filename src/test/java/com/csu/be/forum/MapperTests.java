package com.csu.be.forum;

import com.csu.be.forum.dao.DiscussPostMapper;
import com.csu.be.forum.dao.UserMapper;
import com.csu.be.forum.entity.DiscussPost;
import com.csu.be.forum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @author nql
 * @version 1.0
 * @date 2020/12/29 16:05
 */
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(102);
        System.out.println(user);

        user = userMapper.selectByName("aaa");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder117@sina.com");
        System.out.println(user);
    }

    @Test
    public void testFindUserById(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }


    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate(){
        userMapper.updateStatus(150,1);
    }

    @Test
    public void testDiscussPost(){
        int count = discussPostMapper.selectDiscussRows(111);
        System.out.println(count);

        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(111, 0, 10);
        System.out.println(discussPosts);
    }

}
