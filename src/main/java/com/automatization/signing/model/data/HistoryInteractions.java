package com.automatization.signing.model.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@Builder
public class HistoryInteractions {

    @Id
    private String id;
    @DocumentReference
    private Chats ChatId;
    private String message;
    private LocalDateTime created;
}
