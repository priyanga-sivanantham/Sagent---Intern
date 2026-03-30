package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Chat;
import com.project.eventplan.Entity.Message;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.ChatParticipantRepository;
import com.project.eventplan.Repository.ChatRepository;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.MessageRepository;
import com.project.eventplan.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final EventTeamMemberRepository eventTeamMemberRepository;
    private final EventVendorRepository eventVendorRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public MessageService(MessageRepository messageRepository,
                          ChatRepository chatRepository,
                          ChatParticipantRepository chatParticipantRepository,
                          EventTeamMemberRepository eventTeamMemberRepository,
                          EventVendorRepository eventVendorRepository,
                          UserRepository userRepository,
                          SecurityUtil securityUtil) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.eventTeamMemberRepository = eventTeamMemberRepository;
        this.eventVendorRepository = eventVendorRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public Message sendMessage(Message message) {
        User currentUser = userRepository.findByEmail(securityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Chat chat = chatRepository.findById(message.getChat().getChatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        validateChatAccess(chat, currentUser.getEmail());
        message.setChat(chat);
        message.setSender(currentUser);
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByChat(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        validateChatAccess(chat, securityUtil.getCurrentUserEmail());
        return messageRepository.findByChatChatId(chatId);
    }

    private void validateChatAccess(Chat chat, String email) {
        if (chatParticipantRepository.existsByChatChatIdAndUserEmail(chat.getChatId(), email)) {
            return;
        }

        if (chat.getEvent() != null && chat.getEvent().getEventId() != null) {
            Long eventId = chat.getEvent().getEventId();
            if ((chat.getEvent().getOrganizer() != null && email.equals(chat.getEvent().getOrganizer().getEmail()))
                    || eventTeamMemberRepository.existsByEvent_EventIdAndUser_Email(eventId, email)
                    || eventVendorRepository.existsByEvent_EventIdAndUser_Email(eventId, email)) {
                return;
            }
        }

        throw new AccessDeniedException("Access denied");
    }
}
