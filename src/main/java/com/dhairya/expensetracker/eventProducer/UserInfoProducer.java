package com.dhairya.expensetracker.eventProducer;

import com.dhairya.expensetracker.model.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.*;


@Service
public class UserInfoProducer {

    @Value("${spring.kafka.topic.name}")
    private String TOPIC_NAME;

    private final KafkaTemplate<String, UserInfoDto> kafkaTemplate;
    @Autowired
    public UserInfoProducer(final KafkaTemplate<String, UserInfoDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendEventToKafka(UserInfoDto userInfoDto) {
        Message<UserInfoDto> message = MessageBuilder
                .withPayload(userInfoDto)
                .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                .build();

        kafkaTemplate.send(message);
    }



}
