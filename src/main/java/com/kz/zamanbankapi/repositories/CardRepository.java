package com.kz.zamanbankapi.repositories;

import com.kz.zamanbankapi.dao.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    java.util.Optional<Card> findByUserId(Long userId);
}
