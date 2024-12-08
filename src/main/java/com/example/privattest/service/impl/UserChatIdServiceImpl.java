package com.example.privattest.service.impl;

import com.example.privattest.model.UserChatId;
import com.example.privattest.repository.UserChatIdRepository;
import com.example.privattest.service.UserChatIdService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserChatIdServiceImpl implements UserChatIdService {
    private final UserChatIdRepository userChatIdRepository;

    @Override
    public void saveChatId(Long chatId, String username) {
        log.debug("Saving chat ID for user: {} with chat ID: {}", username, chatId);
        UserChatId userChatId = new UserChatId(chatId, username);
        try {
            userChatIdRepository.save(userChatId);
            log.info("Successfully saved chat ID for user: {}", username);
        } catch (Exception e) {
            log.error("Couldn't save user chat id: {}", e.getMessage());
        }
    }

    @Override
    public List<UserChatId> getUsersChatIds() {
        log.debug("Fetching all user chat IDs from the repository.");
        return userChatIdRepository.findAll();
    }
}
