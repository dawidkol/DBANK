package pl.dk.loanservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static pl.dk.loanservice.kafka.KafkaConstants.CREATE_LOAN_ACCOUNT;

@Configuration
class KafkaConfig {

    @Bean
    public NewTopic createAccountTopic() {
        return TopicBuilder.name(CREATE_LOAN_ACCOUNT)
                .replicas(3)
                .partitions(3)
                .build();
    }
}
