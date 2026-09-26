package com.epg.corebank.Services;

import com.epg.corebank.Controllers.MainController;
import com.epg.corebank.Models.Account;
import com.epg.corebank.Models.Transaction;
import com.epg.corebank.Repositories.AccountRepository;
import com.epg.corebank.Repositories.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account create(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    public Transaction deposit(Long accountId, MainController.@Valid DepositRequest request) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal newBalance = account.getBalance().add(request.amount());
        account.setBalance(newBalance);

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType("DEPOSIT");
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(request.description());
        transaction.setCreatedAt(Instant.now());

        return transactionRepository.save(transaction);
    }

}