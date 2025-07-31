package com.cherniva.transferservice.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfer/sync")
@RequiredArgsConstructor
public class BankAccountController {
    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;

    @PostMapping("/create")
    public ResponseEntity<Void> createAccount(@RequestBody AccountDto accountDto) {
        try {
            updateAccount(accountDto);

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
        Account account = accountMapper.accountDtoToAccount(accountDto);
        return accountRepo.save(account);
    }
}
