package com.cherniva.common.example;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.mapper.UserAccountMapper;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.Currency;
import com.cherniva.common.dto.UserAccountResponseDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class MapperUsageExample {

//    private final UserAccountMapper userAccountMapper;
//    private final AccountMapper accountMapper;
//
//    public MapperUsageExample(UserAccountMapper userAccountMapper, AccountMapper accountMapper) {
//        this.userAccountMapper = userAccountMapper;
//        this.accountMapper = accountMapper;
//    }
//
//    public void demonstrateMapping() {
//        // Create sample data
//        Currency usd = new Currency();
//        usd.setId(1L);
//        usd.setCode("USD");
//        usd.setName("US Dollar");
//
//        Currency eur = new Currency();
//        eur.setId(2L);
//        eur.setCode("EUR");
//        eur.setName("Euro");
//
//        Account account1 = new Account();
//        account1.setId(1L);
//        account1.setCurrency(usd);
//
//        Account account2 = new Account();
//        account2.setId(2L);
//        account2.setCurrency(eur);
//
//        UserDetails userDetails = new UserDetails();
//        userDetails.setId(1L);
//        userDetails.setUsername("john.doe");
//        userDetails.setName("john");
//        userDetails.setSurname("doe");
//        userDetails.setBirthday(LocalDate.of(1990, 1, 1));
//        userDetails.setAccounts(Arrays.asList(account1, account2));
//
//        // Map User to DTO
//        UserAccountResponseDto dto = userAccountMapper.userToUserAccountResponse(userDetails);
//        System.out.println("Mapped DTO: " + dto.getUserId() + " - " + dto.getName());
//
//        // Map individual account
//        AccountDto accountDto = accountMapper.accountToAccountDto(account1);
//        System.out.println("Account DTO: " + accountDto.getCurrencyCode());
//
//        // Map collection
//        List<UserDetails> userDetailsList = Arrays.asList(userDetails);
//        List<UserAccountResponseDto> dtos = userAccountMapper.usersToUserAccountResponses(userDetailsList);
//        System.out.println("Collection mapping: " + dtos.size() + " users mapped");
//    }
} 