package com.automatization.signing.repository;

import com.automatization.signing.model.data.Chats;
import com.automatization.signing.model.data.WaitingList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaitingListRepository extends MongoRepository<WaitingList, String> {

    Optional<WaitingList> findByChatId(Chats chats);
}
