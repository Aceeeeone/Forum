package com.csu.be.forum;

import com.csu.be.forum.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author nql
 * @version 1.0
 * @date 2020/2/20 23:44
 */
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Test
    public void Test(){
        mailClient.sendMail("253977964@qq.com", "Test", "Welcome.");
    }
}
