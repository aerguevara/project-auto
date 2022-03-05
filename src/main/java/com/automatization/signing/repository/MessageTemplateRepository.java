package com.automatization.signing.repository;

import com.automatization.signing.model.data.MessageTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends MongoRepository<MessageTemplate, String> {

    Optional<MessageTemplate> findByInput(String input);
}
