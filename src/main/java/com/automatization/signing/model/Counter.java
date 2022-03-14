package com.automatization.signing.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document
@Builder
public class Counter {

    @Id
    private String id;
    private LocalDate created;
    private LocalDateTime lastModify;
    private int fail;
    private int success;
    private List<LocalDateTime> successDate;
}
