package dev.jrsantos.jms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SendController {

    private final JmsTemplate jmsTemplate;

    public SendController(@Qualifier("receiverJmsTemplate") JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @PostMapping("/send")
    public String send(@RequestBody String message) {
        log.info("Sending message: {}", message);
        jmsTemplate.convertAndSend(message);

        return message;
    }
}
