package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(User.Role.USER)
                .active(true)
                .build();
    }

    @Test
    void findByEmail_Success() {
        entityManager.persist(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("test@test.com");

        assertTrue(found.isPresent());
        assertEquals(testUser.getEmail(), found.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");
        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_Success() {
        entityManager.persist(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals(testUser.getUsername(), found.get().getUsername());
    }

    @Test
    void existsByEmail_True() {
        entityManager.persist(testUser);
        entityManager.flush();

        Boolean exists = userRepository.existsByEmail("test@test.com");
        assertTrue(exists);
    }

    @Test
    void existsByEmail_False() {
        Boolean exists = userRepository.existsByEmail("nonexistent@test.com");
        assertFalse(exists);
    }

    @Test
    void existsByUsername_True() {
        entityManager.persist(testUser);
        entityManager.flush();

        Boolean exists = userRepository.existsByUsername("testuser");
        assertTrue(exists);
    }

    @Test
    void existsByUsername_False() {
        Boolean exists = userRepository.existsByUsername("nonexistent");
        assertFalse(exists);
    }

    @Test
    void findByRole_Success() {
        User adminUser = User.builder()
                .username("admin")
                .email("admin@test.com")
                .password("password123")
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        entityManager.persist(testUser);
        entityManager.persist(adminUser);
        entityManager.flush();

        List<User> users = userRepository.findByRole(User.Role.USER);
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());

        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        assertEquals(1, admins.size());
        assertEquals("admin", admins.get(0).getUsername());
    }

    @Test
    void findByActiveTrue_Success() {
        User inactiveUser = User.builder()
                .username("inactive")
                .email("inactive@test.com")
                .password("password123")
                .firstName("Inactive")
                .lastName("User")
                .role(User.Role.USER)
                .active(false)
                .build();

        entityManager.persist(testUser);
        entityManager.persist(inactiveUser);
        entityManager.flush();

        List<User> activeUsers = userRepository.findByActiveTrue();
        assertEquals(1, activeUsers.size());
        assertTrue(activeUsers.get(0).getActive());
    }

    @Test
    void save_Success() {
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
    }

    @Test
    void delete_Success() {
        entityManager.persist(testUser);
        entityManager.flush();
        Long userId = testUser.getId();

        userRepository.deleteById(userId);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }
}