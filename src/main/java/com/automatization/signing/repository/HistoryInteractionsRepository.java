package com.automatization.signing.repository;

import com.automatization.signing.model.data.HistoryInteractions;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryInteractionsRepository extends MongoRepository<HistoryInteractions, String> {
}
