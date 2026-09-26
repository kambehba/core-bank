package com.epg.corebank.Controllers;

import com.epg.corebank.Models.Account;
import com.epg.corebank.Models.User;
import com.epg.corebank.Models.Transaction;
import com.epg.corebank.Services.AccountService;
import com.epg.corebank.Services.JwtService;
import com.epg.corebank.Services.UserService;
import com.epg.corebank.dto.AuthResponse;
import com.epg.corebank.dto.LoginRequest;
import com.epg.corebank.dto.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.epg.corebank.dto.DepositRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class MainController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AccountService accountService;

    public MainController(UserService userService,AccountService accountService,JwtService jwtService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User savedUser = userService.register(request);

        // Don't echo the password back, even hashed
        savedUser.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/users")
    public ResponseEntity<List<String>> getUsers() {

        return ResponseEntity.ok(List.of("Alice", "Bob", "Charlie"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }


    //@PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("hasAuthority('ADMIN','ROLE_ADMIN')")
    //@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")

    @GetMapping("/users2")
    //@PreAuthorize("hasAuthority('ADMIN')")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsers2() {
        List<User> users = userService.findAll();

        users.forEach(user -> user.setPassword(null));

        return ResponseEntity.ok(users);
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        Account savedAccount = accountService.create(account);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }
    
    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<Transaction> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request
    ) {
        Transaction transaction = accountService.deposit(accountId,request);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    public record DepositRequest(
            @NotNull
            @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
            BigDecimal amount,

            String description
    ) {
    }
}