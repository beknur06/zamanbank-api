package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dao.repositories.CardRepository;
import com.kz.zamanbankapi.dao.repositories.TransactionRepository;
import com.kz.zamanbankapi.dto.TransactionCreateRequest;
import com.kz.zamanbankapi.dto.TransactionDto;
import com.kz.zamanbankapi.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;

    public TransactionDto create(TransactionCreateRequest request) {
        Card sender = cardRepository.findById(request.getSenderCardId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender card not found"));
        Card receiver = cardRepository.findByPhoneNumber(request.getReceiverPhone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender card not found"));
        
        Transaction entity = new Transaction();
        
        entity.setSenderCard(sender);
        entity.setReceiverCard(receiver);
        entity.setAmount(request.getAmount());
        entity.setMessage(request.getMessage());
        
        Transaction saved = transactionRepository.save(entity);
        
        return transactionMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByDay() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return transactionRepository
                .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)
                .stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    public void putReportMessageToTransaction(String reportMessage, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transaction.setReport(reportMessage);
    }
}