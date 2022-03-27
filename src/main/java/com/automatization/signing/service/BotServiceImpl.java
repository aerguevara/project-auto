package com.automatization.signing.service;

import com.automatization.signing.model.NotificationRQDTO;
import com.automatization.signing.model.UniqueRQDTO;
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
    @Value("${app.bot.url-unique}")
    private String urlBotUnique;
    @Value("${app.job.id}")
    private String jobId;
    @Value("${app.bot.channel-id}")
    private String channelId;

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

    @Override
    @Async
    public void sendNotificationUnique(String message) {
        restTemplate.postForObject(urlBotUnique, UniqueRQDTO.builder()
                        .chatId(channelId)
                        .message(message)
                        .build(),
                void.class);
    }
}
