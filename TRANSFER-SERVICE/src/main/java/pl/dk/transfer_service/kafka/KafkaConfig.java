package pl.dk.transfer_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static pl.dk.transfer_service.kafka.KafkaConstants.TOPIC_REGISTRATION;

@Configuration
class KafkaConfig {

    @Bean
    public NewTopic registrationEvents() {
        return TopicBuilder.name(TOPIC_REGISTRATION)
                .partitions(3)
                .replicas(3)
                .build();
    }

}
