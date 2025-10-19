package com.kz.zamanbankapi.dao.repositories;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllBySenderCardOrReceiverCardAndCreatedAtBetween(
            Card senderCard,
            Card receiverCard,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    List<Transaction> findAllBySenderCardId(Long cardId);
}