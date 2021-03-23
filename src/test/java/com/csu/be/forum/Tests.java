package com.csu.be.forum;

import com.csu.be.forum.dao.DiscussPostMapper;
import com.csu.be.forum.entity.Comment;
import com.csu.be.forum.entity.User;
import com.csu.be.forum.service.CommentService;
import com.csu.be.forum.service.FollowService;
import com.csu.be.forum.util.SensitiveFilter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.SpringVersion;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.concurrent.ListenableFuture;

import javax.sound.midi.Soundbank;
import java.util.List;

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

    @Autowired
    private Producer producer;

    @Autowired
    private Consumer consumer;

    @Autowired
    private CommentService commentService;

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
    public void commentTest(){
        int userCommentCount = commentService.findUserCommentCount(111);
        List<Comment> list = commentService.findUserComments(111, 0, 10);
        for (Comment comment : list) {
            System.out.println(comment.toString());
        }
    }

    @Test
    public void followeeTest(){
        long followeeCount = followService.findFolloweeCount(154, 3);
        System.out.println(followeeCount);
    }


    @Test
    public void testKafka(){
        producer.sendMessage("test", "hello world");
        producer.sendMessage("test", "good bye");

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

@Component
class Producer{

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

@Component
class Consumer{

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println("消费者" + Thread.currentThread().getName() + "收到消息：" + record.value());
    }
}

