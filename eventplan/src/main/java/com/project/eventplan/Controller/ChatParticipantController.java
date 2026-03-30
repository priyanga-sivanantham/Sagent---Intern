package com.project.eventplan.Controller;

import com.project.eventplan.Dto.ChatParticipantRequest;
import com.project.eventplan.Entity.Chat;
import com.project.eventplan.Entity.ChatParticipant;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Service.ChatParticipantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat-participants")
public class ChatParticipantController {

    private final ChatParticipantService chatParticipantService;

    public ChatParticipantController(ChatParticipantService chatParticipantService) {
        this.chatParticipantService = chatParticipantService;
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @PostMapping
    public ChatParticipant addParticipant(@Valid @RequestBody ChatParticipantRequest request) {
        ChatParticipant participant = new ChatParticipant();
        Chat chat = new Chat();
        chat.setChatId(request.chatId());
        participant.setChat(chat);

        User user = new User();
        user.setUserId(request.userId());
        participant.setUser(user);
        return chatParticipantService.addParticipant(participant);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping
    public List<ChatParticipant> getAllParticipants() {
        return chatParticipantService.getAllParticipants();
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping("/chat/{chatId}")
    public List<ChatParticipant> getParticipants(@PathVariable Long chatId) {
        return chatParticipantService.getParticipantsByChat(chatId);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @DeleteMapping("/{id}")
    public void removeParticipant(@PathVariable Long id) {
        chatParticipantService.removeParticipant(id);
    }
}
