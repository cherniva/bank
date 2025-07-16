package com.cherniva.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExchangeRateDto {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDateTime lastUpdated;
} 