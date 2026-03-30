package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Chat;
import com.project.eventplan.Entity.ChatParticipant;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.BadRequestException;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.ChatParticipantRepository;
import com.project.eventplan.Repository.ChatRepository;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final EventRepository eventRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final EventTeamMemberRepository eventTeamMemberRepository;
    private final EventVendorRepository eventVendorRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public ChatService(ChatRepository chatRepository,
                       EventRepository eventRepository,
                       ChatParticipantRepository chatParticipantRepository,
                       EventTeamMemberRepository eventTeamMemberRepository,
                       EventVendorRepository eventVendorRepository,
                       UserRepository userRepository,
                       SecurityUtil securityUtil) {
        this.chatRepository = chatRepository;
        this.eventRepository = eventRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.eventTeamMemberRepository = eventTeamMemberRepository;
        this.eventVendorRepository = eventVendorRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public Chat createChat(Chat chat) {
        String email = securityUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (chat.getChatType() == null) {
            throw new BadRequestException("Chat type is required");
        }

        if (chat.getEvent() != null) {
            Long eventId = chat.getEvent().getEventId();
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

            if (!hasEventAccess(eventId, email, event)) {
                throw new AccessDeniedException("Access denied");
            }

            chat.setEvent(event);
        }

        Chat savedChat = chatRepository.save(chat);
        ensureParticipant(savedChat, currentUser);
        return savedChat;
    }

    public List<Chat> getAllChats() {
        String email = securityUtil.getCurrentUserEmail();
        return chatRepository.findAll().stream()
                .filter(chat -> canAccessChat(chat, email))
                .collect(Collectors.toList());
    }

    private boolean canAccessChat(Chat chat, String email) {
        if (chat.getChatId() != null && chatParticipantRepository.existsByChatChatIdAndUserEmail(chat.getChatId(), email)) {
            return true;
        }

        if (chat.getEvent() == null || chat.getEvent().getEventId() == null) {
            return false;
        }

        return hasEventAccess(chat.getEvent().getEventId(), email, chat.getEvent());
    }

    private boolean hasEventAccess(Long eventId, String email, Event event) {
        return (event.getOrganizer() != null && email.equals(event.getOrganizer().getEmail()))
                || eventTeamMemberRepository.existsByEvent_EventIdAndUser_Email(eventId, email)
                || eventVendorRepository.existsByEvent_EventIdAndUser_Email(eventId, email);
    }

    private void ensureParticipant(Chat chat, User user) {
        if (!chatParticipantRepository.existsByChatChatIdAndUserUserId(chat.getChatId(), user.getUserId())) {
            ChatParticipant participant = new ChatParticipant();
            participant.setChat(chat);
            participant.setUser(user);
            chatParticipantRepository.save(participant);
        }
    }
}
