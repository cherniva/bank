package com.cherniva.accountsservice.controller;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.dto.TransferDto;
import com.cherniva.common.mapper.AccountMapper;
import com.cherniva.common.model.Account;
import com.cherniva.common.repo.AccountRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    @Transactional
    public ResponseEntity<Void> transfer(@RequestBody TransferDto transferDto) {
        try {
            Account sourceAccount = accountMapper.accountDtoToAccount(transferDto.getSourceAccount());
            Account destinationAccount = accountMapper.accountDtoToAccount(transferDto.getDestinationAccount());

            accountRepo.save(sourceAccount);
            accountRepo.save(destinationAccount);

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
