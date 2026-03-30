package com.project.eventplan.Controller;

import com.project.eventplan.Dto.MessageRequest;
import com.project.eventplan.Entity.Chat;
import com.project.eventplan.Entity.Message;
import com.project.eventplan.Service.MessageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @PostMapping
    public Message sendMessage(@Valid @RequestBody MessageRequest request) {
        Message message = new Message();
        message.setContent(request.content());
        Chat chat = new Chat();
        chat.setChatId(request.chatId());
        message.setChat(chat);
        return messageService.sendMessage(message);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping("/chat/{chatId}")
    public List<Message> getMessages(@PathVariable Long chatId) {
        return messageService.getMessagesByChat(chatId);
    }
}
