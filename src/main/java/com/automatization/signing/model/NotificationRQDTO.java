package com.automatization.signing.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Setter
@Getter
@Builder
@Jacksonized
public class NotificationRQDTO {
    private String jobId;
    private String message;
}
