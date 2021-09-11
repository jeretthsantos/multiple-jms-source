package dev.jrsantos.jms.config;

import dev.jrsantos.jms.property.ReceiverJmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import java.util.Properties;

@RequiredArgsConstructor
@Configuration
public class ReceiverJmsConfiguration {

    private final ReceiverJmsProperties properties;

    @Primary
    @Bean("receiverJmsListenerCachingConnectionFactory")
    public CachingConnectionFactory cacheConnectionFactory(
            @Qualifier("receiverConnectionFactory") ConnectionFactory connectionFactory) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setTargetConnectionFactory(connectionFactory);
        factory.setReconnectOnException(true);
        factory.setSessionCacheSize(500);

        return factory;
    }

    @Bean("receiverJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory receiverJmsListenerContainerFactory(
            @Qualifier("receiverConnectionFactory") ConnectionFactory connectionFactory,
            @Qualifier("receiverDestinationResolver") DestinationResolver destinationResolver) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(destinationResolver);
        factory.setConcurrency(properties.getMinConsumersCount() + "-" + properties.getMaxConsumersCount());
        factory.setReceiveTimeout(properties.getTimeout());

        return factory;
    }

    @Bean("receiverJndiTemplate")
    public JndiTemplate receiverJndiTemplate() {
        Properties environment = new Properties();
        environment.put("java.naming.factory.initial", properties.getJndiFactory());
        environment.put("java.naming.provider.url", properties.getProviderUrl());
        environment.put("java.naming.security.principal", properties.getUsername());
        environment.put("java.naming.security.credentials", properties.getPassword());

        return new JndiTemplate(environment);
    }

    @Bean("receiverConnectionFactory")
    public JndiObjectFactoryBean receiverConnectionFactory(
            @Qualifier("receiverJndiTemplate") JndiTemplate jndiTemplate) {
        JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
        factory.setJndiTemplate(jndiTemplate);
        factory.setJndiName(properties.getJndiName());
        factory.setProxyInterface(QueueConnectionFactory.class);

        return factory;
    }

    @Bean("receiverDestinationResolver")
    public JndiDestinationResolver activeMQDestinationResolver(
            @Qualifier("receiverJndiTemplate") JndiTemplate jndiTemplate) {
        JndiDestinationResolver resolver = new JndiDestinationResolver();
        resolver.setJndiTemplate(jndiTemplate);

        return resolver;
    }

    @Bean("receiverJmsTemplate")
    public JmsTemplate receiverJmsTemplate(
            @Qualifier("receiverConnectionFactory") ConnectionFactory connectionFactory,
            @Qualifier("receiverDestinationResolver") DestinationResolver destinationResolver) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setDestinationResolver(destinationResolver);
        template.setDefaultDestinationName(properties.getDestination());

        return template;
    }
}
