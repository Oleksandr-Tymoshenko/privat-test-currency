package com.example.privattest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.privattest.model.UserChatId;
import com.example.privattest.repository.UserChatIdRepository;
import com.example.privattest.service.impl.UserChatIdServiceImpl;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserChatIdServiceImplTest {
    @Mock
    private UserChatIdRepository userChatIdRepository;

    @InjectMocks
    private UserChatIdServiceImpl userChatIdService;

    @Test
    @DisplayName("saveChatId - valid input - successfully saves chat ID")
    void saveChatId_whenValidInput_savesChatIdSuccessfully() {
        // Given
        Long chatId = 12345L;
        String username = "testUser";

        // When
        userChatIdService.saveChatId(chatId, username);

        // Then
        verify(userChatIdRepository, times(1)).save(any(UserChatId.class));
    }

    @Test
    @DisplayName("getUsersChatIds - successfully fetches chat IDs")
    void getUsersChatIds_whenDataExists_returnsListOfChatIds() {
        // Given
        UserChatId userChatId1 = new UserChatId(12345L, "user1");
        UserChatId userChatId2 = new UserChatId(67890L, "user2");
        when(userChatIdRepository.findAll()).thenReturn(List.of(userChatId1, userChatId2));

        // When
        List<UserChatId> result = userChatIdService.getUsersChatIds();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(userChatId1));
        assertTrue(result.contains(userChatId2));
    }

    @Test
    @DisplayName("getUsersChatIds - no data - returns empty list")
    void getUsersChatIds_whenNoData_returnsEmptyList() {
        // Given
        when(userChatIdRepository.findAll()).thenReturn(List.of());

        // When
        List<UserChatId> result = userChatIdService.getUsersChatIds();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
