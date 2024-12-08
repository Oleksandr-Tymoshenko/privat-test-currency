package com.example.privattest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user_chat_id")
@NoArgsConstructor
@AllArgsConstructor
public class UserChatId {
    @Id
    private Long chatId;

    @Column(nullable = false)
    private String username;
}
