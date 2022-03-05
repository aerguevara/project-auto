package com.automatization.signing.model.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@Builder
public class MessageTemplate {

    @Id
    private String id;
    @Indexed(unique = true)
    private String input;
    private String output;
}
