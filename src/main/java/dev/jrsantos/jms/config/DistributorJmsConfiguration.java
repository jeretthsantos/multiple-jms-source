package dev.jrsantos.jms.config;

import dev.jrsantos.jms.property.DistributorJmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class DistributorJmsConfiguration {

    private final DistributorJmsProperties properties;

    @Bean("distributorJmsListenerCachingConnectionFactory")
    public CachingConnectionFactory cacheConnectionFactory(@Qualifier("distributorConnectionFactory") ConnectionFactory connectionFactory) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setTargetConnectionFactory(connectionFactory);
        factory.setReconnectOnException(true);
        factory.setSessionCacheSize(500);

        return factory;
    }

    @Bean("distributorJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory distributorJmsListenerContainerFactory(
            @Qualifier("distributorConnectionFactory") ConnectionFactory distributorConnectionFactory,
            @Qualifier("distributorDestinationResolver") DestinationResolver distributorDestinationResolver) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(distributorConnectionFactory);
        factory.setDestinationResolver(distributorDestinationResolver);
        factory.setConcurrency(properties.getMinConsumersCount() + "-" + properties.getMaxConsumersCount());
        factory.setReceiveTimeout(properties.getTimeout());

        return factory;
    }

    @Bean("distributorJndiTemplate")
    public JndiTemplate distributorJndiTemplate() {
        Properties environment = new Properties();
        environment.put("java.naming.factory.initial", properties.getJndiFactory());
        environment.put("java.naming.provider.url", properties.getProviderUrl());
        environment.put("java.naming.security.principal", properties.getUsername());
        environment.put("java.naming.security.credentials", properties.getPassword());

        return new JndiTemplate(environment);
    }

    @Bean("distributorConnectionFactory")
    public JndiObjectFactoryBean distributorConnectionFactory(
            @Qualifier("distributorJndiTemplate") JndiTemplate jndiTemplate) {
        JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
        factory.setJndiTemplate(jndiTemplate);
        factory.setJndiName(properties.getJndiName());
        factory.setProxyInterface(QueueConnectionFactory.class);

        return factory;
    }

    @Bean("distributorDestinationResolver")
    public JndiDestinationResolver distributorDestinationResolver(
            @Qualifier("distributorJndiTemplate") JndiTemplate jndiTemplate) {
        JndiDestinationResolver resolver = new JndiDestinationResolver();
        resolver.setJndiTemplate(jndiTemplate);

        return resolver;
    }

    @Bean("distributorJmsTemplate")
    public List<JmsTemplate> distributorJmsTemplates(
            @Qualifier("distributorConnectionFactory") ConnectionFactory connectionFactory,
            @Qualifier("distributorDestinationResolver") DestinationResolver destinationResolver) {

        return properties.getDestinations()
                .stream()
                .map(destination -> {
                    JmsTemplate template = new JmsTemplate(connectionFactory);
                    template.setDestinationResolver(destinationResolver);
                    template.setDefaultDestinationName(destination);

                    return template;
                })
                .collect(Collectors.toList());
    }
}
