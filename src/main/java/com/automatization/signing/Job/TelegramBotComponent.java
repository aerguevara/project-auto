package com.automatization.signing.Job;

import com.automatization.signing.model.data.Chats;
import com.automatization.signing.service.ChatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Component
@Slf4j
public class TelegramBotComponent extends TelegramLongPollingBot {

    private static final String TOKEN_NIMBUS_BOT = "5239816618:AAHI-gJ9b4MJCy2xFZA1BTGsesNJP6aIvAg";

    private final ChatsService chatsService;

    public TelegramBotComponent(ChatsService chatsService) {
        this.chatsService = chatsService;
    }

    @Override
    public String getBotUsername() {
        return "NimbusGodBot";
    }

    @Override
    public String getBotToken() {
        return TOKEN_NIMBUS_BOT;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final String msg = update.getMessage().getText();
        final Long chatId = update.getMessage().getChatId();
        String output = chatsService.findOutputByInput(Chats.builder()
                        .chatId(chatId.toString())
                        .firstName(update
                                .getMessage()
                                .getFrom()
                                .getFirstName())
                        .build(),
                msg);
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(output);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ocurrio un error al enviar el mensaje", e);
        }
    }

    public void sendPhoto(File file, String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(file));
        sendPhoto.setChatId(chatId);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Ocurrio un error al enviar el mensaje", e);
        }

    }
}
