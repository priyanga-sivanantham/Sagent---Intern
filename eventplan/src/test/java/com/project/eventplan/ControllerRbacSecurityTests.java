package com.project.eventplan;

import com.project.eventplan.Controller.BudgetController;
import com.project.eventplan.Controller.ChatController;
import com.project.eventplan.Controller.ChatParticipantController;
import com.project.eventplan.Controller.EventController;
import com.project.eventplan.Controller.EventTeamMemberController;
import com.project.eventplan.Controller.EventVendorController;
import com.project.eventplan.Controller.ExpenseController;
import com.project.eventplan.Controller.FeedbackController;
import com.project.eventplan.Controller.GuestController;
import com.project.eventplan.Controller.MessageController;
import com.project.eventplan.Controller.TaskController;
import com.project.eventplan.Controller.UserController;
import com.project.eventplan.Controller.VendorController;
import com.project.eventplan.Config.JwtUtil;
import com.project.eventplan.Exception.BadRequestException;
import com.project.eventplan.Service.BudgetService;
import com.project.eventplan.Service.ChatParticipantService;
import com.project.eventplan.Service.ChatService;
import com.project.eventplan.Service.EventService;
import com.project.eventplan.Service.EventTeamMemberService;
import com.project.eventplan.Service.EventVendorService;
import com.project.eventplan.Service.ExpenseService;
import com.project.eventplan.Service.FeedbackService;
import com.project.eventplan.Service.GuestService;
import com.project.eventplan.Service.MessageService;
import com.project.eventplan.Service.TaskService;
import com.project.eventplan.Service.UserService;
import com.project.eventplan.Service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
        UserController.class,
        EventController.class,
        TaskController.class,
        VendorController.class,
        BudgetController.class,
        FeedbackController.class,
        ChatController.class,
        EventTeamMemberController.class,
        EventVendorController.class,
        ExpenseController.class,
        GuestController.class,
        ChatParticipantController.class,
        MessageController.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.project.eventplan.Config.JwtFilter.class
        )
)
@Import(ControllerRbacSecurityTests.TestSecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=test-secret-key-test-secret-key-1234")
class ControllerRbacSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private VendorService vendorService;
    @MockitoBean
    private BudgetService budgetService;
    @MockitoBean
    private FeedbackService feedbackService;
    @MockitoBean
    private ChatService chatService;
    @MockitoBean
    private EventTeamMemberService eventTeamMemberService;
    @MockitoBean
    private EventVendorService eventVendorService;
    @MockitoBean
    private ExpenseService expenseService;
    @MockitoBean
    private GuestService guestService;
    @MockitoBean
    private ChatParticipantService chatParticipantService;
    @MockitoBean
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        when(userService.getCurrentUser()).thenReturn(null);
        when(eventService.getEvent(anyLong())).thenReturn(null);
        when(eventService.getEventsForCurrentUser()).thenReturn(Collections.emptyList());
        when(taskService.getTaskById(anyLong())).thenReturn(null);
        when(taskService.getTasksForCurrentUser()).thenReturn(Collections.emptyList());
        when(taskService.updateTaskStatus(anyLong(), anyString())).thenReturn(null);
        when(vendorService.createVendor(any())).thenReturn(null);
        when(vendorService.getAllVendors()).thenReturn(Collections.emptyList());
        when(vendorService.updateVendor(anyLong(), any())).thenReturn(null);
        when(budgetService.getAllBudgets()).thenReturn(Collections.emptyList());
        when(feedbackService.getAllFeedback()).thenReturn(Collections.emptyList());
        when(chatService.getAllChats()).thenReturn(Collections.emptyList());
        when(eventTeamMemberService.getAllMembers()).thenReturn(Collections.emptyList());
        when(eventVendorService.assignVendor(anyLong(), anyLong(), anyString())).thenReturn(null);
        when(eventVendorService.getAllAssignments()).thenReturn(Collections.emptyList());
        when(expenseService.getAllExpenses()).thenReturn(Collections.emptyList());
        when(guestService.getAllGuests(any())).thenReturn(Collections.emptyList());
        when(guestService.respondToInvitation(anyString())).thenReturn(null);
        when(chatParticipantService.getAllParticipants()).thenReturn(Collections.emptyList());
        when(messageService.getMessagesByChat(anyLong())).thenReturn(Collections.emptyList());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/events/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void guestResponseLinkIsPublic() throws Exception {
        mockMvc.perform(get("/guests/respond").param("token", "signed-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/guests/invitation").param("token", "signed-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/feedback/form").param("token", "signed-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/feedback/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"signed-token\",\"rating\":5,\"comments\":\"Great\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void organizerCanAccessOrganizerOnlyData() throws Exception {
        mockMvc.perform(get("/users").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/budgets").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/feedback").with(role("ORGANIZER")))
                .andExpect(status().isOk());
    }

    @Test
    void teamMemberCanViewAssignedEventAndFeedbackButNotBudget() throws Exception {
        mockMvc.perform(get("/events").with(role("TEAM_MEMBER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/events/1").with(role("TEAM_MEMBER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/budgets").with(role("TEAM_MEMBER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/feedback").with(role("TEAM_MEMBER")))
                .andExpect(status().isOk());
    }

    @Test
    void vendorCanViewAssignedEventAndFeedbackButNotTasksOrBudget() throws Exception {
        mockMvc.perform(get("/events/1").with(role("VENDOR")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tasks").with(role("VENDOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/budgets").with(role("VENDOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/feedback").with(role("VENDOR")))
                .andExpect(status().isOk());
    }

    @Test
    void organizerCanAssignTasksButTeamMemberCannotCreateThem() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .with(role("ORGANIZER"))
                        .param("eventId", "1")
                        .param("teamMemberId", "2")
                        .param("vendorId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskName\":\"Setup stage\",\"description\":\"Prepare the venue\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/tasks/create")
                        .with(role("TEAM_MEMBER"))
                        .param("eventId", "1")
                        .param("teamMemberId", "2")
                        .param("vendorId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskName\":\"Setup stage\",\"description\":\"Prepare the venue\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void teamMemberCanManageTasksButVendorCannotUpdateTaskStatus() throws Exception {
        mockMvc.perform(get("/tasks").with(role("TEAM_MEMBER")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/tasks/1/status")
                        .with(role("TEAM_MEMBER"))
                        .param("status", "ONGOING"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/tasks/1/status")
                        .with(role("VENDOR"))
                        .param("status", "ONGOING"))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendorManagementMatchesRoleRules() throws Exception {
        mockMvc.perform(post("/vendors")
                        .with(role("TEAM_MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vendorName\":\"Acme\",\"serviceType\":\"Catering\",\"contact\":\"9999999999\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendors").with(role("VENDOR")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/vendors/1")
                        .with(role("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vendorName\":\"Acme\",\"serviceType\":\"Catering\",\"contact\":\"9999999999\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void chatEndpointsAreAvailableToAllRoles() throws Exception {
        mockMvc.perform(get("/chats").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/chat-participants").with(role("TEAM_MEMBER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/messages/chat/1").with(role("VENDOR")))
                .andExpect(status().isOk());
    }

    @Test
    void organizerOwnsTeamMemberAndVendorAssignmentControllers() throws Exception {
        mockMvc.perform(get("/eventteammembers").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/eventteammembers").with(role("TEAM_MEMBER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/eventvendors/assign")
                        .with(role("ORGANIZER"))
                        .param("eventId", "1")
                        .param("vendorId", "2")
                        .param("serviceDetails", "Sound"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/eventvendors/assign")
                        .with(role("VENDOR"))
                        .param("eventId", "1")
                        .param("vendorId", "2")
                        .param("serviceDetails", "Sound"))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerOwnsGuestAndExpenseManagement() throws Exception {
        mockMvc.perform(get("/guests").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/guests").with(role("TEAM_MEMBER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/expenses").with(role("ORGANIZER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/expenses").with(role("VENDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void corsPreflightAllowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/users/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void runtimeExceptionsReturnStructuredJsonErrors() throws Exception {
        when(userService.login(anyString(), anyString()))
                .thenThrow(new BadRequestException("Invalid password"));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor role(String role) {
        return SecurityMockMvcRequestPostProcessors.user(role.toLowerCase())
                .roles(role);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .cors(Customizer.withDefaults())
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.POST, "/users/register", "/users/login", "/users/refresh").permitAll()
                            .requestMatchers(HttpMethod.GET, "/guests/respond", "/guests/invitation", "/feedback/form").permitAll()
                            .requestMatchers(HttpMethod.POST, "/guests/respond", "/feedback/submit").permitAll()
                            .anyRequest().authenticated()
                    )
                    .httpBasic(Customizer.withDefaults());

            return http.build();
        }

        @Bean
        org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
            org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
            configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            configuration.setAllowedHeaders(Collections.singletonList("*"));
            configuration.setAllowCredentials(true);

            org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                    new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }
}
