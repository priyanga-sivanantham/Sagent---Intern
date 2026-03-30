package com.project.eventplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.eventplan.Config.JwtUtil;
import com.project.eventplan.Entity.*;
import com.project.eventplan.Entity.Enums.ChatType;
import com.project.eventplan.Entity.Enums.Role;
import com.project.eventplan.Repository.*;
import com.project.eventplan.Service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void cleanDatabase() {
        reset(emailService);
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
    void authEndpointsWorkWithRealDatabase() throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Organizer One",
                                  "email":"organizer@example.com",
                                  "password":"Password123",
                                  "phone":"9999999999",
                                  "role":"ORGANIZER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("organizer@example.com"));

        MvcResult loginResult = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"organizer@example.com",
                                  "password":"Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("organizer@example.com"))
                .andReturn();

        String token = readJson(loginResult).get("token").asText();

        mockMvc.perform(get("/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("organizer@example.com"));

        mockMvc.perform(post("/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void organizerCanManageUsersAndEvents() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        String organizerToken = login("organizer@example.com", "Password123");

        mockMvc.perform(post("/users")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Member One",
                                  "email":"member@example.com",
                                  "password":"Password123",
                                  "phone":"8888888888",
                                  "role":"TEAM_MEMBER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("member@example.com"));

        mockMvc.perform(get("/users")
                        .header("Authorization", bearer(organizerToken))
                        .param("query", "member")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email").value("member@example.com"));

        MvcResult eventResult = mockMvc.perform(post("/events")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventName":"Spring Launch",
                                  "eventDate":"2030-04-01",
                                  "venue":"Main Hall"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName").value("Spring Launch"))
                .andReturn();

        long eventId = readJson(eventResult).get("eventId").asLong();

        mockMvc.perform(get("/events")
                        .header("Authorization", bearer(organizerToken))
                        .param("query", "spring")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].eventId").value(eventId));

        mockMvc.perform(put("/events/" + eventId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventName":"Spring Launch Updated",
                                  "eventDate":"2030-04-02",
                                  "venue":"Conference Center"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue").value("Conference Center"));

        mockMvc.perform(get("/events/" + eventId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId));
    }

    @Test
    void organizerCanManageAssignmentsTasksAndVendors() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        User teamMember = createUser("Team", "team@example.com", Role.TEAM_MEMBER);
        User vendorUser = createUser("Vendor", "vendor@example.com", Role.VENDOR);
        Event event = createEvent("Annual Meet", organizer);
        String organizerToken = login("organizer@example.com", "Password123");
        String teamToken = login("team@example.com", "Password123");

        MvcResult vendorResult = mockMvc.perform(post("/vendors")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vendorName":"Acme Catering",
                                  "serviceType":"Catering",
                                  "contact":"7777777777",
                                  "userId":%d
                                }
                                """.formatted(vendorUser.getUserId())))
                .andExpect(status().isOk())
                .andReturn();

        long vendorId = readJson(vendorResult).get("vendorId").asLong();

        mockMvc.perform(get("/vendors")
                        .header("Authorization", bearer(organizerToken))
                        .param("query", "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].vendorId").value(vendorId));

        MvcResult assignmentResult = mockMvc.perform(post("/eventteammembers")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId":%d,
                                  "userId":%d,
                                  "roleInEvent":"Coordinator"
                                }
                                """.formatted(event.getEventId(), teamMember.getUserId())))
                .andExpect(status().isOk())
                .andReturn();

        long memberAssignmentId = readJson(assignmentResult).get("id").asLong();

        mockMvc.perform(get("/eventteammembers")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(memberAssignmentId));

        MvcResult eventVendorResult = mockMvc.perform(post("/eventvendors/assign")
                        .header("Authorization", bearer(organizerToken))
                        .param("eventId", String.valueOf(event.getEventId()))
                        .param("vendorId", String.valueOf(vendorUser.getUserId()))
                        .param("serviceDetails", "Full-service catering"))
                .andExpect(status().isOk())
                .andReturn();

        long eventVendorId = readJson(eventVendorResult).get("id").asLong();

        mockMvc.perform(get("/eventvendors")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventVendorId));

        MvcResult taskResult = mockMvc.perform(post("/tasks/create")
                        .header("Authorization", bearer(organizerToken))
                        .param("eventId", String.valueOf(event.getEventId()))
                        .param("teamMemberId", String.valueOf(teamMember.getUserId()))
                        .param("vendorId", String.valueOf(vendorUser.getUserId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskName":"Prepare stage",
                                  "description":"Stage and lights"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        long taskId = readJson(taskResult).get("taskId").asLong();

        mockMvc.perform(get("/tasks")
                        .header("Authorization", bearer(teamToken))
                        .param("status", "NOT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].taskId").value(taskId));

        mockMvc.perform(put("/tasks/" + taskId + "/status")
                        .header("Authorization", bearer(teamToken))
                        .param("status", "ONGOING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONGOING"));
    }

    @Test
    void organizerCanManageBudgetExpenseGuestsAndFeedback() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        Event event = createEvent("Budget Event", organizer);
        String organizerToken = login("organizer@example.com", "Password123");

        MvcResult budgetResult = mockMvc.perform(post("/budgets")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "totalBudget":10000,
                                  "remainingBudget":8000,
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn();

        long budgetId = readJson(budgetResult).get("budgetId").asLong();

        mockMvc.perform(get("/budgets")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].budgetId").value(budgetId));

        mockMvc.perform(post("/expenses")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expenseName":"Venue booking",
                                  "amount":2500,
                                  "description":"Main hall rent",
                                  "budgetId":%d
                                }
                                """.formatted(budgetId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expenseName").value("Venue booking"));

        mockMvc.perform(get("/expenses")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].expenseName").value("Venue booking"));

        MvcResult guestResult = mockMvc.perform(post("/guests")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestName":"Guest One",
                                  "email":"guest@example.com",
                                  "phone":"6666666666",
                                  "rsvpStatus":"Pending",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn();

        long guestId = readJson(guestResult).get("guestId").asLong();

        mockMvc.perform(get("/guests")
                        .header("Authorization", bearer(organizerToken))
                        .param("query", "guest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].guestId").value(guestId));

        mockMvc.perform(post("/guests/send-invitations/" + event.getEventId())
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        verify(emailService, times(1))
                .sendEmail(eq("guest@example.com"), contains("Invitation"), contains("/invitation.html?token="));

        String invitationToken = jwtUtil.generateGuestInvitationToken(guestId);

        mockMvc.perform(get("/guests/invitation").param("token", invitationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(event.getEventId()))
                .andExpect(jsonPath("$.availableStatuses[0]").value("ACCEPTED"));

        mockMvc.perform(post("/guests/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "status":"ACCEPTED"
                                }
                                """.formatted(invitationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsvpStatus").value("Accepted"));

        mockMvc.perform(post("/guests/" + event.getEventId() + "/send-feedback-mail")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        verify(emailService, times(1))
                .sendEmail(eq("guest@example.com"), contains("We value your feedback"), contains("/feedback.html?token="));

        String feedbackToken = jwtUtil.generateFeedbackToken(guestId, event.getEventId());

        mockMvc.perform(get("/feedback/form").param("token", feedbackToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestId").value(guestId));

        mockMvc.perform(post("/feedback/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "rating":5,
                                  "comments":"Great event"
                                }
                                """.formatted(feedbackToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/feedback")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comments").value("Great event"));
    }

    @Test
    void chatAndMessagingApisWorkAcrossParticipants() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        User teamMember = createUser("Team", "team@example.com", Role.TEAM_MEMBER);
        User vendorUser = createUser("Vendor", "vendor@example.com", Role.VENDOR);
        Event event = createEvent("Chat Event", organizer);
        createTeamAssignment(event, teamMember, "Coordinator");
        createVendorAssignment(event, vendorUser, "Sound");

        String organizerToken = login("organizer@example.com", "Password123");
        String vendorToken = login("vendor@example.com", "Password123");

        MvcResult chatResult = mockMvc.perform(post("/chats")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chatName":"Ops Room",
                                  "chatType":"GROUP",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn();

        long chatId = readJson(chatResult).get("chatId").asLong();

        MvcResult participantResult = mockMvc.perform(post("/chat-participants")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chatId":%d,
                                  "userId":%d
                                }
                                """.formatted(chatId, vendorUser.getUserId())))
                .andExpect(status().isOk())
                .andReturn();

        long participantId = readJson(participantResult).get("id").asLong();

        mockMvc.perform(get("/chats")
                        .header("Authorization", bearer(organizerToken))
                        .param("query", "ops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].chatId").value(chatId));

        mockMvc.perform(get("/chat-participants/chat/" + chatId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(participantId));

        mockMvc.perform(post("/messages")
                        .header("Authorization", bearer(vendorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content":"Vendor joined",
                                  "chatId":%d
                                }
                                """.formatted(chatId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Vendor joined"));

        mockMvc.perform(get("/messages/chat/" + chatId)
                        .header("Authorization", bearer(vendorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Vendor joined"));
    }

    @Test
    void acceptedGuestsOnlyReceiveFeedbackEmailsAndAssignedRolesCanViewFeedback() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        User teamMember = createUser("Team", "team@example.com", Role.TEAM_MEMBER);
        User vendorUser = createUser("Vendor", "vendor@example.com", Role.VENDOR);
        Event event = createEvent("Feedback Event", organizer);
        createTeamAssignment(event, teamMember, "Ops");
        createVendorAssignment(event, vendorUser, "Food");
        String organizerToken = login("organizer@example.com", "Password123");
        String teamToken = login("team@example.com", "Password123");
        String vendorToken = login("vendor@example.com", "Password123");

        long acceptedGuestId = readJson(mockMvc.perform(post("/guests")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestName":"Accepted Guest",
                                  "email":"accepted@example.com",
                                  "phone":"6111111111",
                                  "rsvpStatus":"Accepted",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn()).get("guestId").asLong();

        mockMvc.perform(post("/guests")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestName":"Pending Guest",
                                  "email":"pending@example.com",
                                  "phone":"6222222222",
                                  "rsvpStatus":"Pending",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/guests/" + event.getEventId() + "/send-feedback-mail")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        verify(emailService, times(1))
                .sendEmail(eq("accepted@example.com"), contains("We value your feedback"), contains("/feedback.html?token="));

        String feedbackToken = jwtUtil.generateFeedbackToken(acceptedGuestId, event.getEventId());
        mockMvc.perform(post("/feedback/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "rating":4,
                                  "comments":"Well managed"
                                }
                                """.formatted(feedbackToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/feedback/event/" + event.getEventId())
                        .header("Authorization", bearer(teamToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comments").value("Well managed"));

        mockMvc.perform(get("/feedback/event/" + event.getEventId())
                        .header("Authorization", bearer(vendorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comments").value("Well managed"));
    }

    @Test
    void updateAndDeleteEndpointsWorkAcrossResources() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        User teamMember = createUser("Team", "team@example.com", Role.TEAM_MEMBER);
        User vendorUser = createUser("Vendor", "vendor@example.com", Role.VENDOR);
        Event event = createEvent("Lifecycle Event", organizer);
        String organizerToken = login("organizer@example.com", "Password123");

        long createdUserId = readJson(mockMvc.perform(post("/users")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Second Member",
                                  "email":"second@example.com",
                                  "password":"Password123",
                                  "phone":"8111111111",
                                  "role":"TEAM_MEMBER"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()).get("userId").asLong();

        mockMvc.perform(put("/users/" + createdUserId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Second Member Updated",
                                  "email":"second@example.com",
                                  "password":"Password123",
                                  "phone":"8222222222",
                                  "role":"TEAM_MEMBER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("8222222222"));

        MvcResult vendorResult = mockMvc.perform(post("/vendors")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vendorName":"Delete Me Vendor",
                                  "serviceType":"Lighting",
                                  "contact":"7333333333",
                                  "userId":%d
                                }
                                """.formatted(vendorUser.getUserId())))
                .andExpect(status().isOk())
                .andReturn();
        long vendorId = readJson(vendorResult).get("vendorId").asLong();

        mockMvc.perform(put("/vendors/" + vendorId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vendorName":"Updated Vendor",
                                  "serviceType":"Lighting",
                                  "contact":"7444444444",
                                  "userId":%d
                                }
                                """.formatted(vendorUser.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendorName").value("Updated Vendor"));

        long budgetId = readJson(mockMvc.perform(post("/budgets")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "totalBudget":9000,
                                  "remainingBudget":9000,
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn()).get("budgetId").asLong();

        mockMvc.perform(put("/budgets/" + budgetId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "totalBudget":12000,
                                  "remainingBudget":10000,
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudget").value(12000));

        long expenseId = readJson(mockMvc.perform(post("/expenses")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expenseName":"Initial Expense",
                                  "amount":500,
                                  "description":"Desc",
                                  "budgetId":%d
                                }
                                """.formatted(budgetId)))
                .andExpect(status().isOk())
                .andReturn()).get("expenseId").asLong();

        mockMvc.perform(put("/expenses/" + expenseId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expenseName":"Updated Expense",
                                  "amount":750,
                                  "description":"Updated",
                                  "budgetId":%d
                                }
                                """.formatted(budgetId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(750));

        long guestId = readJson(mockMvc.perform(post("/guests")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestName":"Guest To Update",
                                  "email":"guest2@example.com",
                                  "phone":"6555555555",
                                  "rsvpStatus":"Pending",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn()).get("guestId").asLong();

        mockMvc.perform(put("/guests/" + guestId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestName":"Guest Updated",
                                  "email":"guest2@example.com",
                                  "phone":"6555555555",
                                  "rsvpStatus":"Accepted",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsvpStatus").value("Accepted"));

        String feedbackToken = jwtUtil.generateFeedbackToken(guestId, event.getEventId());
        long feedbackId = readJson(mockMvc.perform(post("/feedback/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "rating":4,
                                  "comments":"To delete"
                                }
                                """.formatted(feedbackToken)))
                .andExpect(status().isOk())
                .andReturn()).get("feedbackId").asLong();

        long teamAssignmentId = readJson(mockMvc.perform(post("/eventteammembers")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId":%d,
                                  "userId":%d,
                                  "roleInEvent":"Setup"
                                }
                                """.formatted(event.getEventId(), teamMember.getUserId())))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();

        mockMvc.perform(put("/eventteammembers/" + teamAssignmentId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId":%d,
                                  "userId":%d,
                                  "roleInEvent":"Lead"
                                }
                                """.formatted(event.getEventId(), teamMember.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleInEvent").value("Lead"));

        long eventVendorId = readJson(mockMvc.perform(post("/eventvendors/assign")
                        .header("Authorization", bearer(organizerToken))
                        .param("eventId", String.valueOf(event.getEventId()))
                        .param("vendorId", String.valueOf(vendorUser.getUserId()))
                        .param("serviceDetails", "Catering"))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();

        long taskId = readJson(mockMvc.perform(post("/tasks/create")
                        .header("Authorization", bearer(organizerToken))
                        .param("eventId", String.valueOf(event.getEventId()))
                        .param("teamMemberId", String.valueOf(teamMember.getUserId()))
                        .param("vendorId", String.valueOf(vendorUser.getUserId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskName":"Delete Task",
                                  "description":"Desc"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()).get("taskId").asLong();

        long chatId = readJson(mockMvc.perform(post("/chats")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chatName":"Delete Participant Chat",
                                  "chatType":"GROUP",
                                  "eventId":%d
                                }
                                """.formatted(event.getEventId())))
                .andExpect(status().isOk())
                .andReturn()).get("chatId").asLong();

        long participantId = readJson(mockMvc.perform(post("/chat-participants")
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chatId":%d,
                                  "userId":%d
                                }
                                """.formatted(chatId, vendorUser.getUserId())))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();

        mockMvc.perform(delete("/chat-participants/" + participantId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/eventvendors/" + eventVendorId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/eventteammembers/" + teamAssignmentId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/feedback/" + feedbackId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/tasks/" + taskId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/expenses/" + expenseId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/budgets/" + budgetId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/guests/" + guestId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vendors/" + vendorId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/" + createdUserId)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());
    }

    @Test
    void apiNegativePathsAndSwaggerEndpointsAreCovered() throws Exception {
        User organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        User vendorUser = createUser("Vendor", "vendor@example.com", Role.VENDOR);
        String organizerToken = login("organizer@example.com", "Password123");
        String vendorToken = login("vendor@example.com", "Password123");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"",
                                  "email":"bad-email",
                                  "password":"123",
                                  "phone":"abc",
                                  "role":null
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users/99999")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/budgets")
                        .header("Authorization", bearer(vendorToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invalid-token\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    private User createUser(String name, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setPhone("9000000000");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Event createEvent(String name, User organizer) {
        Event event = new Event();
        event.setEventName(name);
        event.setEventDate(LocalDate.of(2030, 1, 1));
        event.setVenue("Venue");
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    private void createTeamAssignment(Event event, User user, String roleInEvent) {
        EventTeamMember assignment = new EventTeamMember();
        assignment.setEvent(event);
        assignment.setUser(user);
        assignment.setRoleInEvent(roleInEvent);
        eventTeamMemberRepository.save(assignment);
    }

    private void createVendorAssignment(Event event, User user, String serviceDetails) {
        EventVendor assignment = new EventVendor();
        assignment.setEvent(event);
        assignment.setUser(user);
        assignment.setServiceDetails(serviceDetails);
        eventVendorRepository.save(assignment);
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
        return readJson(result).get("token").asText();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
