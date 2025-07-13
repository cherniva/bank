package com.cherniva.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.beans.propertyeditors.CurrencyEditor;

import java.math.BigDecimal;

@Entity
@Data
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Currency sourceCurrency;
    private Currency targetCurrency;
    private BigDecimal sellPrice;
    private BigDecimal buyPrice;
}
