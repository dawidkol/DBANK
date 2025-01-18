package pl.dk.accounts_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static pl.dk.accounts_service.kafka.KafkaConstants.*;

@Configuration
class KafkaConfig {

    @Bean
    public NewTopic processTransferTopic() {
        return TopicBuilder.name(PROCESS_TRANSFER_EVENT)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic loanAccountCreated() {
        return TopicBuilder.name(LOAN_ACCOUNT_CREATED)
                .partitions(3)
                .replicas(3)
                .build();
    }

}
