package com.automatization.signing.model.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@Builder
public class Chats {

    @Id
    private String id;
    @Indexed(unique = true)
    private String chatId;
    private String firstName;
    private LocalDateTime lastDateInteraction;
    private LocalDateTime created;

}
