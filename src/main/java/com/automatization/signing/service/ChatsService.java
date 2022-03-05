package com.automatization.signing.service;

import com.automatization.signing.model.data.Chats;

import java.util.Optional;

public interface ChatsService {


    String findOutputByInput(Chats chats, String message);
}
