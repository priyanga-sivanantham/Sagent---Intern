package com.project.eventplan.Controller;

import com.project.eventplan.Dto.ChatRequest;
import com.project.eventplan.Dto.PageResponse;
import com.project.eventplan.Entity.Chat;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Service.ChatService;
import com.project.eventplan.Util.PaginationUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @PostMapping
    public Chat createChat(@Valid @RequestBody ChatRequest request) {
        Chat chat = new Chat();
        chat.setChatName(request.chatName());
        chat.setChatType(request.chatType());
        if (request.eventId() != null) {
            Event event = new Event();
            event.setEventId(request.eventId());
            chat.setEvent(event);
        }
        return chatService.createChat(chat);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping
    public PageResponse<Chat> getChats(@RequestParam(required = false) String query,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        List<Chat> chats = chatService.getAllChats();
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase(Locale.ROOT);
            chats = chats.stream()
                    .filter(chat -> contains(chat.getChatName(), normalized)
                            || (chat.getChatType() != null && chat.getChatType().name().toLowerCase(Locale.ROOT).contains(normalized)))
                    .toList();
        }
        return PaginationUtils.paginate(chats, page, size);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
