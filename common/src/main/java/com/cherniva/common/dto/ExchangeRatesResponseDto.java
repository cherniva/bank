package com.cherniva.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExchangeRatesResponseDto {
    private List<ExchangeRateDto> rates;
    private String message;
    private boolean success;
} 