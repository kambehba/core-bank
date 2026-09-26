package com.epg.corebank.Repositories;

import com.epg.corebank.Models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
