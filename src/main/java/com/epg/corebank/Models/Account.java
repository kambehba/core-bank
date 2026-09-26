package com.epg.corebank.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "accounts", schema = "public", indexes = {@Index(name = "idx_accounts_user_id",
        columnList = "user_id")}, uniqueConstraints = {@UniqueConstraint(name = "accounts_account_number_key",
        columnNames = {"account_number"})})
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Size(max = 20)
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Size(max = 20)
    @NotNull
    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;


}