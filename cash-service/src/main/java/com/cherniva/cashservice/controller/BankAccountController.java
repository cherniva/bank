package com.cherniva.cashservice.controller;

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
@RequestMapping("/cash/sync")
@RequiredArgsConstructor
public class BankAccountController {

    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;

    @PostMapping("/create")
    public ResponseEntity<Void> createAccount(@RequestBody AccountDto accountDto) {
        try {
            Account account = accountMapper.accountDtoToAccount(accountDto);
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

    @PostMapping("/transfer")
    @Transactional(rollbackFor = Exception.class)
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
}
