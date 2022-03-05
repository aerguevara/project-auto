package com.automatization.signing.repository;

import com.automatization.signing.model.data.Chats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.Optional;

@Repository
public interface ChatsRepository extends MongoRepository<Chats, String> {

    Optional<Chats> findByChatId(String chatId);
}
