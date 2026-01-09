package com.jsalva.trainerworkload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
@EnableTransactionManagement
public class ActiveMqConfig {
    private static final Logger logger = LoggerFactory.getLogger(ActiveMqConfig.class);

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;


    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter =
                new MappingJackson2MessageConverter();

        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");  // Must match producer

        // Use simple class name instead of fully qualified name
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("TrainerWorkloadCommandMessageDto", TrainerWorkloadCommandMessageDto.class);
        converter.setTypeIdMappings(typeIdMappings);

        // Configure ObjectMapper for proper date/time handling
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);

        // RedeliveryPolicy on consumer side
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);           // Retry 3 times
        redeliveryPolicy.setInitialRedeliveryDelay(2000);     // 2 second
        redeliveryPolicy.setUseExponentialBackOff(true);      // Exponential backoff
        redeliveryPolicy.setBackOffMultiplier(2.0);           // 2x wait time

        factory.setRedeliveryPolicy(redeliveryPolicy);

        return factory;
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager(ConnectionFactory connectionFactory) {
        return new JmsTransactionManager(connectionFactory);
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            JmsTransactionManager jmsTransactionManager) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        // Enable transactions
        factory.setSessionTransacted(true);
        factory.setTransactionManager(jmsTransactionManager);

        // Acknowledge mode (use Session.SESSION_TRANSACTED with transactions)
        factory.setSessionAcknowledgeMode(jakarta.jms.Session.SESSION_TRANSACTED);

        // Concurrency (number of concurrent consumers)
        factory.setConcurrency("1-5");  // 1-5 consumers

        // Custom Error Handler - logs errors and tracks retries
        factory.setErrorHandler(t -> {
            logger.error("Handling error in listener for messages, error: {}", t.getMessage());
        });

        return factory;
    }
}
