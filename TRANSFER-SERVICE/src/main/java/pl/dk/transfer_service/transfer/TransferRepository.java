package pl.dk.transfer_service.transfer;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
interface TransferRepository extends JpaRepository<Transfer, String> {

    Page<Transfer> findAllBySenderAccountNumber(String senderAccountNumber, Pageable pageable);

    Page<Transfer> findAllBySenderAccountNumberAndRecipientAccountNumber(String senderAccountNumber,
                                                                         String recipientAccountNumber,
                                                                         Pageable pageable);

    List<Transfer> findAllByTransferStatusAndTransferDateBefore(TransferStatus transferStatus, LocalDateTime transferDate);

    Optional<Transfer> findByIdAndTransferStatus(String id, TransferStatus transferStatus);
}
