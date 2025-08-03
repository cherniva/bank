package com.cherniva.transferservice.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.mapper.UserMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.repo.AccountRepo;
import com.cherniva.common.repo.CurrencyRepo;
import com.cherniva.common.repo.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfer/sync")
@RequiredArgsConstructor
public class BankAccountController {
    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final UserDetailsRepo userDetailsRepo;
    private final CurrencyRepo currencyRepo;

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
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/delete")
    public ResponseEntity<Void> deleteAccount(@RequestParam Long accountId) {
        try {
            accountRepo.deleteById(accountId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestBody AccountDto accountDto) {
        try {
            updateAccount(accountDto);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestBody AccountDto accountDto) {
        try {
            updateAccount(accountDto);

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
