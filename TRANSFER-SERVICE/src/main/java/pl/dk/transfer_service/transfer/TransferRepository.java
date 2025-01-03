package pl.dk.transfer_service.transfer;

import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface TransferRepository extends JpaRepository<Transfer, String> {

    Page<Transfer> findAllBySenderAccountNumber(String senderAccountNumber, Pageable pageable);

    Page<Transfer> findAllBySenderAccountNumberAndRecipientAccountNumber(String senderAccountNumber,
                                                                         String recipientAccountNumber,
                                                                         Pageable pageable);
}
