package com.automatization.signing.service;

import com.automatization.signing.model.NotificationRQDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BotServiceImpl implements BotService {
    private final RestTemplate restTemplate;
    @Value("${app.bot.url}")
    private String urlBot;
    @Value("${app.job.id}")
    private String jobId;

    public BotServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Async
    public void sendNotification(String data, boolean onlyAdmin) {
        restTemplate.postForObject(urlBot, NotificationRQDTO.builder()
                        .jobId(jobId)
                        .message(data)
                        .onlyAdmin(onlyAdmin)
                        .build(),
                void.class);
    }
}
