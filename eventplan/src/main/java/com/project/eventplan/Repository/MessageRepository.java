package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatChatId(Long chatId);

}
