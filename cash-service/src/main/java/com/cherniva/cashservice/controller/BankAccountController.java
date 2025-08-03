package com.cherniva.cashservice.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.TransferDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.model.UserDetails;
import com.cherniva.common.repo.AccountRepo;
import com.cherniva.common.repo.CurrencyRepo;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cash/sync")
@RequiredArgsConstructor
@Slf4j
public class BankAccountController {

    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;
    private final UserDetailsRepo userDetailsRepo;
    private final CurrencyRepo currencyRepo;
    private final UserMapper userMapper;

    @GetMapping
    public String hello() {
        return "Hello from cash";
    }

    @PostMapping("/createUser")
    public ResponseEntity<Void> createUser(@RequestBody UserAccountResponseDto userAccountResponseDto) {
        try {
            var userDetails = userMapper.userAccountResponseToUser(userAccountResponseDto);
            for (var account : userDetails.getAccounts()) {
                account.setUserDetails(userDetails);
                var currency = currencyRepo.findCurrencyByCode(account.getCurrency().getCode()).get();
                account.setCurrency(currency);
            }
            userDetailsRepo.save(userDetails);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to create user: {}", userAccountResponseDto, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createAccount(@RequestBody AccountDto accountDto) {
        try {
            var account = accountMapper.accountDtoToAccount(accountDto);
            var userDetails = userDetailsRepo.findById(accountDto.getUserDetailsId()).get();
            var currency = currencyRepo.findCurrencyByCode(accountDto.getCurrencyCode()).get();
            account.setUserDetails(userDetails);
            account.setCurrency(currency);
            accountRepo.save(account);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to create account: {}", accountDto, e);
            return ResponseEntity.badRequest().build();
        }
    }

//    @PutMapping("/delete")
//    public ResponseEntity<Void> deleteAccount(@RequestParam Long accountId) {
//        try {
//            accountRepo.deleteById(accountId);
//
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    @PostMapping("/transfer")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Void> transfer(@RequestBody TransferDto transferDto) {
        try {
            updateAccount(transferDto.getSourceAccount());
            updateAccount(transferDto.getDestinationAccount());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Account updateAccount(AccountDto accountDto) {
        var account = accountRepo.findById(accountDto.getAccountId()).get();
        account.setAmount(accountDto.getAmount());

        return accountRepo.save(account);
    }
}
