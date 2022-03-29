package com.automatization.signing.repository;

import com.automatization.signing.model.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CounterRepository extends MongoRepository<Counter, String> {

    Optional<Counter> findByCreated(LocalDate localDate);

    Optional<Counter> findByActiveIsTrue();
}
