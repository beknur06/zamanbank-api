package com.kz.zamanbankapi.controller;
import com.kz.zamanbankapi.dto.TransactionCreateRequest;
import com.kz.zamanbankapi.dto.TransactionDto;
import com.kz.zamanbankapi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public TransactionDto create(@Valid @RequestBody TransactionCreateRequest request) {
        return transactionService.create(request);
    }

    @GetMapping
    public List<TransactionDto> getTransactionsByDay() {
        return transactionService.getTransactionsByDay();
    }

    @PutMapping
    public void putReportMessageToTransaction(@RequestParam String reportMessage, @RequestParam Long transactionId) {
        transactionService.putReportMessageToTransaction(reportMessage, transactionId);
    }
}