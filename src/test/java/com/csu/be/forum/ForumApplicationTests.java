package com.csu.be.forum;

import com.csu.be.forum.dao.AlphaDao;
import com.csu.be.forum.dao.AlphaDaoMybatisImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class ForumApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Autowired
    private AlphaDao alphaDao;

    @Test
    void contextLoads() {
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void TestDao(){
        System.out.println(alphaDao.find());

        System.out.println(simpleDateFormat.format(new Date()));
    }
}
