package com.project.eventplan.Controller;

import com.project.eventplan.Config.JwtUtil;
import com.project.eventplan.Dto.AuthResponse;
import com.project.eventplan.Dto.PageResponse;
import com.project.eventplan.Dto.RefreshTokenRequest;
import com.project.eventplan.Dto.UserLoginRequest;
import com.project.eventplan.Dto.UserRequest;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Service.UserService;
import com.project.eventplan.Util.PaginationUtils;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody UserRequest request) {
        return userService.register(toUser(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody UserLoginRequest request) {
        User loggedInUser = userService.login(request.email(), request.password());
        return buildAuthResponse(loggedInUser);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String email = jwtUtil.extractEmail(request.token());
            User user = userService.getUserByEmail(email);
            if (!jwtUtil.validateToken(request.token(), user.getEmail())) {
                throw new com.project.eventplan.Exception.BadRequestException("Token is invalid or expired");
            }
            return buildAuthResponse(user);
        } catch (JwtException ex) {
            throw new com.project.eventplan.Exception.BadRequestException("Token is invalid or expired");
        }
    }

    @GetMapping("/me")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public User createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(toUser(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping
    public PageResponse<User> getAllUsers(@RequestParam(required = false) String query,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size) {
        List<User> users = userService.getAllUsers();
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase(Locale.ROOT);
            users = users.stream()
                    .filter(user -> contains(user.getName(), normalized)
                            || contains(user.getEmail(), normalized)
                            || contains(user.getPhone(), normalized))
                    .toList();
        }
        return PaginationUtils.paginate(users, page, size);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, toUser(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    private User toUser(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setPhone(request.phone());
        user.setRole(request.role());
        return user;
    }

    private AuthResponse buildAuthResponse(User user) {
        long expiresInSeconds = jwtUtil.getAuthTokenTtlSeconds();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                token,
                "Bearer",
                expiresInSeconds,
                Instant.now().plusSeconds(expiresInSeconds),
                new AuthResponse.UserSummary(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole()
                )
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
