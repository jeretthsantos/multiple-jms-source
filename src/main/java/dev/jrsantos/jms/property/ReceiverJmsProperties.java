package dev.jrsantos.jms.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("jms.receiver")
public class ReceiverJmsProperties {

    private String jndiFactory;

    private String providerUrl;

    private String jndiName;

    private String username;

    private String password;

    private int minConsumersCount;

    private int maxConsumersCount;

    private Long timeout;

    private String destination;

}
