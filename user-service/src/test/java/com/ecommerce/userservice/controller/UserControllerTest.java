package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.*;
import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse userResponse;
    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER.name())
                .active(true)
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .userId(1L)
                .username("testuser")
                .email("test@test.com")
                .role("USER")
                .build();
    }

    @Test
    void register_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void register_InvalidInput() throws Exception {
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                .username("ab")
                .email("invalid-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void login_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        List<UserResponse> users = Arrays.asList(userResponse, userResponse);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ActiveOnly() throws Exception {
        List<UserResponse> users = Arrays.asList(userResponse);
        when(userService.getActiveUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users?activeOnly=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService, times(1)).getActiveUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_Success() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_Success() throws Exception {
        doNothing().when(userService).deactivateUser(1L);

        mockMvc.perform(patch("/api/users/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deactivateUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_Success() throws Exception {
        doNothing().when(userService).activateUser(1L);

        mockMvc.perform(patch("/api/users/1/activate"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).activateUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void promoteToAdmin_Success() throws Exception {
        when(userService.promoteToAdmin(1L)).thenReturn(userResponse);

        mockMvc.perform(patch("/api/users/admin/promote/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).promoteToAdmin(1L);
    }
}