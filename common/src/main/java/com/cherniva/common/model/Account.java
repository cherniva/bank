package com.cherniva.common.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_details_id")
    private UserDetails userDetails;
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;
}
