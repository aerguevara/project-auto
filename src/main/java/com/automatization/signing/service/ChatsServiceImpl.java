package com.automatization.signing.service;

import com.automatization.signing.model.data.Chats;
import com.automatization.signing.model.data.HistoryInteractions;
import com.automatization.signing.model.data.MessageTemplate;
import com.automatization.signing.model.data.WaitingList;
import com.automatization.signing.repository.ChatsRepository;
import com.automatization.signing.repository.HistoryInteractionsRepository;
import com.automatization.signing.repository.MessageTemplateRepository;
import com.automatization.signing.repository.WaitingListRepository;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ChatsServiceImpl implements ChatsService {

    private static final String DEFAULT_RESPONSE = "Lo siento {0}, no te estoy entendiendo, me da ansiedad!!";
    public static final String JOINLIST = "/joinlist";

    private final ChatsRepository chatsRepository;
    private final HistoryInteractionsRepository historyInteractionsRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private final WaitingListRepository waitingListRepository;

    public ChatsServiceImpl(ChatsRepository chatsRepository,
                            HistoryInteractionsRepository historyInteractionsRepository,
                            MessageTemplateRepository messageTemplateRepository,
                            WaitingListRepository waitingListRepository) {
        this.chatsRepository = chatsRepository;
        this.historyInteractionsRepository = historyInteractionsRepository;
        this.messageTemplateRepository = messageTemplateRepository;
        this.waitingListRepository = waitingListRepository;
    }

    public Chats save(Chats chats, String message) {
        return chatsRepository.findByChatId(chats.getChatId())
                .map(chatsPresent -> {
                    chatsPresent.setLastDateInteraction(LocalDateTime.now());
                    saveHistory(message, chatsPresent);
                    return chatsRepository.save(chatsPresent);
                })
                .orElseGet(() -> {
                            Chats chatsSave = chatsRepository.save(
                                    Chats.builder()
                                            .chatId(chats.getChatId())
                                            .firstName(chats.getFirstName())
                                            .lastDateInteraction(LocalDateTime.now())
                                            .created(LocalDateTime.now())
                                            .build());
                            saveHistory(message, chatsSave);
                            return chatsSave;

                        }
                );
    }

    @Override
    public String findOutputByInput(Chats chats, String message) {
        Chats chatsSave = save(chats, message);
        String parameter = Optional.ofNullable(filterActionCommand(chatsSave, message))
                .orElseGet(chats::getFirstName);
        return
                messageTemplateRepository
                        .findByInput(message)
                        .map(MessageTemplate::getOutput)
                        .map(output -> MessageFormat.format(output, parameter))
                        .orElseGet(() -> MessageFormat.format(DEFAULT_RESPONSE, parameter));


    }

    private String filterActionCommand(Chats chats, String message) {
        return Optional.ofNullable(message)
                .filter(command -> command.equalsIgnoreCase(JOINLIST))
                .map(command ->
                        waitingListRepository.findByChatId(chats)
                                .map(waitingList -> String.valueOf(waitingList.getPosition()))
                                .orElseGet(() -> {
                                    long position = waitingListRepository.count() + 1;
                                    waitingListRepository.save(WaitingList.builder()
                                            .chatId(chats)
                                            .position(position)
                                            .build());
                                    return String.valueOf(position);
                                }))
                .orElse(null);


    }


    private void saveHistory(String message, Chats chatsPresent) {
        historyInteractionsRepository.save(
                HistoryInteractions.builder()
                        .ChatId(chatsPresent)
                        .created(LocalDateTime.now())
                        .message(message)
                        .build());
    }
}
