package pl.dk.transfer_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static pl.dk.transfer_service.kafka.KafkaConstants.CREATE_TRANSFER_EVENT;
import static pl.dk.transfer_service.kafka.KafkaConstants.TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS;

@Configuration
class KafkaConfig {

    @Bean
    public NewTopic registrationEvents() {
        return TopicBuilder.name(CREATE_TRANSFER_EVENT)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic createTransferServiceTopic() {
        return TopicBuilder.name(TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS)
                .replicas(3)
                .partitions(3)
                .build();
    }

}
