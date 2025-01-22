package pl.dk.exchange_service.exchange;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ExchangeRepository extends JpaRepository<Exchange, String> {
    Page<Exchange> findAllByAccountNumber(@NotBlank @NotBlank String accountNumber, Pageable pageable);
}
