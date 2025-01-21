package pl.dk.exchange_service.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dk.exchange_service.enums.CurrencyType;

import java.util.Optional;


@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    Optional<Currency> findFirstByCurrencyType(CurrencyType currencyType);

}
