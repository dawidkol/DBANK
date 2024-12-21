package pl.dk.accounts_service.kafka.producer;

enum TransferStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
