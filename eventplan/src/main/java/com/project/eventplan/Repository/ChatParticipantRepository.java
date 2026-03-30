package com.project.eventplan.Repository;

import com.project.eventplan.Entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByChatChatId(Long chatId);
    List<ChatParticipant> findByUserEmail(String email);
    boolean existsByChatChatIdAndUserEmail(Long chatId, String email);
    boolean existsByChatChatIdAndUserUserId(Long chatId, Long userId);

}
