package com.cherniva.accountsservice.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.TransferDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts/sync")
@RequiredArgsConstructor
public class BankAccountController {

    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;

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
