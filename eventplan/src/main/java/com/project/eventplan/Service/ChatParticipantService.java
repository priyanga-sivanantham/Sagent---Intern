package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Chat;
import com.project.eventplan.Entity.ChatParticipant;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.ChatParticipantRepository;
import com.project.eventplan.Repository.ChatRepository;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRepository chatRepository;
    private final EventTeamMemberRepository eventTeamMemberRepository;
    private final EventVendorRepository eventVendorRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public ChatParticipantService(ChatParticipantRepository chatParticipantRepository,
                                  ChatRepository chatRepository,
                                  EventTeamMemberRepository eventTeamMemberRepository,
                                  EventVendorRepository eventVendorRepository,
                                  UserRepository userRepository,
                                  SecurityUtil securityUtil) {
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRepository = chatRepository;
        this.eventTeamMemberRepository = eventTeamMemberRepository;
        this.eventVendorRepository = eventVendorRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public ChatParticipant addParticipant(ChatParticipant participant) {
        Chat chat = loadChat(participant.getChat().getChatId());
        User user = userRepository.findById(participant.getUser().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateChatAccess(chat);

        if (chatParticipantRepository.existsByChatChatIdAndUserUserId(chat.getChatId(), user.getUserId())) {
            return chatParticipantRepository.findByChatChatId(chat.getChatId()).stream()
                    .filter(existingParticipant -> existingParticipant.getUser() != null
                            && user.getUserId().equals(existingParticipant.getUser().getUserId()))
                    .findFirst()
                    .orElseGet(() -> {
                        participant.setChat(chat);
                        participant.setUser(user);
                        return chatParticipantRepository.save(participant);
                    });
        }

        participant.setChat(chat);
        participant.setUser(user);
        return chatParticipantRepository.save(participant);
    }

    public List<ChatParticipant> getAllParticipants() {
        String email = securityUtil.getCurrentUserEmail();
        return chatParticipantRepository.findAll().stream()
                .filter(participant -> canAccessChat(participant.getChat(), email))
                .collect(Collectors.toList());
    }

    public List<ChatParticipant> getParticipantsByChat(Long chatId) {
        validateChatAccess(loadChat(chatId));
        return chatParticipantRepository.findByChatChatId(chatId);
    }

    public void removeParticipant(Long id) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        validateChatAccess(participant.getChat());
        chatParticipantRepository.deleteById(id);
    }

    private Chat loadChat(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
    }

    private void validateChatAccess(Chat chat) {
        if (!canAccessChat(chat, securityUtil.getCurrentUserEmail())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private boolean canAccessChat(Chat chat, String email) {
        if (chat == null) {
            return false;
        }

        if (chat.getChatId() != null && chatParticipantRepository.existsByChatChatIdAndUserEmail(chat.getChatId(), email)) {
            return true;
        }

        if (chat.getEvent() == null || chat.getEvent().getEventId() == null) {
            return false;
        }

        Long eventId = chat.getEvent().getEventId();
        return (chat.getEvent().getOrganizer() != null && email.equals(chat.getEvent().getOrganizer().getEmail()))
                || eventTeamMemberRepository.existsByEvent_EventIdAndUser_Email(eventId, email)
                || eventVendorRepository.existsByEvent_EventIdAndUser_Email(eventId, email);
    }
}
