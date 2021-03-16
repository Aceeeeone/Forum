package com.csu.be.forum.event;

import com.alibaba.fastjson.JSONObject;
import com.csu.be.forum.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author nql
 * @version 1.0
 * @date 2020/3/12 17:33
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 事件发布到主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
