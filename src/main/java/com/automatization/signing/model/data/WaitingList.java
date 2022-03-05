package com.automatization.signing.model.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document
@Getter
@Setter
@Builder
public class WaitingList {

    private String id;
    @DocumentReference
    private Chats chatId;
    private long position;


}
