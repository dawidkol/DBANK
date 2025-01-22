package pl.dk.exchange_service.exchange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ExchangeRepository extends JpaRepository<Exchange, String> {
}
