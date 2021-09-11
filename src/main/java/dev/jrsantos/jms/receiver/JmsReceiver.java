package dev.jrsantos.jms.receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JmsReceiver {

    private final List<JmsTemplate> jmsTemplates;

    public JmsReceiver(@Qualifier("distributorJmsTemplate") List<JmsTemplate> jmsTemplates) {
        this.jmsTemplates = jmsTemplates;
    }

    @JmsListener(
            containerFactory = "receiverJmsListenerContainerFactory",
            destination = "#{receiverJmsProperties.destination}")
    public void receive(String message) {
        log.info("Message receive: {}", message);
        jmsTemplates.forEach(template -> template.convertAndSend(message));
    }

}
