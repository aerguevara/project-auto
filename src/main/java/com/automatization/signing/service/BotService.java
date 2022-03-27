package com.automatization.signing.service;

public interface BotService {

    void sendNotification(String data, boolean onlyAdmin);

    void sendNotificationUnique(String message);
}
