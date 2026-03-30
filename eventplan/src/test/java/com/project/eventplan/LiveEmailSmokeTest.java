package com.project.eventplan;

import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.Enums.Role;
import com.project.eventplan.Entity.Guest;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Repository.BudgetRepository;
import com.project.eventplan.Repository.ChatParticipantRepository;
import com.project.eventplan.Repository.ChatRepository;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.ExpenseRepository;
import com.project.eventplan.Repository.FeedbackRepository;
import com.project.eventplan.Repository.GuestRepository;
import com.project.eventplan.Repository.MessageRepository;
import com.project.eventplan.Repository.TaskRepository;
import com.project.eventplan.Repository.UserRepository;
import com.project.eventplan.Repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "liveMailTest", matches = "true")
class LiveEmailSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private EventTeamMemberRepository eventTeamMemberRepository;

    @Autowired
    private EventVendorRepository eventVendorRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Value("${mail.smoke.recipient:${spring.mail.username}}")
    private String recipientEmail;

    @BeforeEach
    void cleanDatabase() {
        messageRepository.deleteAll();
        chatParticipantRepository.deleteAll();
        chatRepository.deleteAll();
        feedbackRepository.deleteAll();
        expenseRepository.deleteAll();
        budgetRepository.deleteAll();
        taskRepository.deleteAll();
        eventVendorRepository.deleteAll();
        eventTeamMemberRepository.deleteAll();
        guestRepository.deleteAll();
        vendorRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void sendsRealInvitationEmail() throws Exception {
        User organizer = new User();
        organizer.setName("Mail Organizer");
        organizer.setEmail("mail-organizer@example.com");
        organizer.setPassword(passwordEncoder.encode("Password123"));
        organizer.setPhone("9000000000");
        organizer.setRole(Role.ORGANIZER);
        organizer = userRepository.save(organizer);

        Event event = new Event();
        event.setEventName("SMTP Smoke Event");
        event.setEventDate(LocalDate.of(2030, 5, 1));
        event.setVenue("Mail Test Venue");
        event.setOrganizer(organizer);
        event = eventRepository.save(event);

        Guest guest = new Guest();
        guest.setGuestName("Smoke Guest");
        guest.setEmail(recipientEmail);
        guest.setPhone("9111111111");
        guest.setRsvpStatus("Pending");
        guest.setEvent(event);
        guestRepository.save(guest);

        String token = login("mail-organizer@example.com", "Password123");

        mockMvc.perform(post("/guests/send-invitations/" + event.getEventId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        int start = body.indexOf("\"token\":\"") + 9;
        int end = body.indexOf('"', start);
        return body.substring(start, end);
    }
}
