package com.kz.zamanbankapi.dao.repositories;

import com.kz.zamanbankapi.dao.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    java.util.Optional<Card> findByPhoneNumber(String phoneNumber);
}
