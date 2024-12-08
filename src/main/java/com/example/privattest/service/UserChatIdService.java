package com.example.privattest.service;

import com.example.privattest.model.UserChatId;
import java.util.List;

/**
 * Service interface for managing user chat IDs.
 * Provides methods to save a chat ID and retrieve all stored chat IDs for users.
 */
public interface UserChatIdService {
    /**
     * Saves a user's chat ID along with their username.
     *
     * @param chatId   the unique chat ID of the user
     * @param username the username of the user associated with the chat ID
     */
    void saveChatId(Long chatId, String username);

    /**
     * Retrieves a list of all user chat IDs.
     *
     * @return a list of {@link UserChatId} objects, each containing a user's chat ID and username
     */
    List<UserChatId> getUsersChatIds();
}
