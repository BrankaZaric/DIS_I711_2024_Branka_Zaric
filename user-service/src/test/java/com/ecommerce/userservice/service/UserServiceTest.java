package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.*;
import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(User.Role.USER)
                .active(true)
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .phoneNumber("9876543210")
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.registerUser(registrationRequest);

        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(registrationRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(registrationRequest));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserById(1L));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserByEmail("test@test.com");

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(testUser, testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void getActiveUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByActiveTrue()).thenReturn(users);

        List<UserResponse> responses = userService.getActiveUsers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getActive());
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .email("updated@test.com")
                .firstName("Updated")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(1L, updateRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deactivateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUser(1L);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void activateUser_Success() {
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.activateUser(1L);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(1L));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void promoteToAdmin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.promoteToAdmin(1L);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }
}