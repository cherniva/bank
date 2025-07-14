package com.cherniva.common.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.propertyeditors.CurrencyEditor;

import java.math.BigDecimal;

@Entity
@Data
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Currency sourceCurrency;

    @ManyToOne
    private Currency targetCurrency;

    private BigDecimal sellPrice;
    private BigDecimal buyPrice;
}
