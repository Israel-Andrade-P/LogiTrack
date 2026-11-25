package com.zeldev.tracking_service.config;

import com.zeldev.tracking_service.event.OrderCreatedEvent;
import com.zeldev.tracking_service.event.OrderStatusUpdateEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties
    ) {
        var deserializer = new JacksonJsonDeserializer<>(OrderCreatedEvent.class);
        deserializer.ignoreTypeHeaders();

        return createFactory(kafkaProperties, deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderStatusUpdateEvent> orderStatusUpdateKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties
    ) {
        var deserializer = new JacksonJsonDeserializer<>(OrderStatusUpdateEvent.class);
        deserializer.ignoreTypeHeaders();

        return createFactory(kafkaProperties, deserializer);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createFactory(
            KafkaProperties kafkaProperties,
            JacksonJsonDeserializer<T> valueDeserializer
    ) {
        ConsumerFactory<String, T> consumerFactory = new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                valueDeserializer
        );

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
