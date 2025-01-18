package pl.dk.exchange_service.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
interface CurrencyRepository extends JpaRepository<Currency, String> {

}
