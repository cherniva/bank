package com.cherniva.common.repo;

import com.cherniva.common.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepo extends JpaRepository<Currency, Long> {
    Optional<Currency> findCurrencyByCode(String code);
}
