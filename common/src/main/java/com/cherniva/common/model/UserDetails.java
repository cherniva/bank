package com.cherniva.common.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthday;
    @OneToMany(mappedBy = "userDetails", cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();
}
