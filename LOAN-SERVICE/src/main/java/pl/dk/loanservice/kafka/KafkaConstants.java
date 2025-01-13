package pl.dk.loanservice.kafka;

public class KafkaConstants {

    public static final String CREATE_LOAN_ACCOUNT = "loan-service-create-loan-account";
    public static final String LOAN_ACCOUNT_CREATED = "account-service-loan-account-created";
    public static final String LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS = "loan-service-update-loan-payment-status";
    public static final String TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS = "transfer-service-update-loan-payment-status";
    public static final String LOAN_SERVICE_TRUSTED_PACKAGE = "pl.dk.loanservice.kafka.consumer.dtos";
}
