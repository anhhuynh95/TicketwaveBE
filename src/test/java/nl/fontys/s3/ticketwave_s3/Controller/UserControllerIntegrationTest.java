package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.transaction.Transactional;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDBRepository userDBRepository;

    @BeforeEach
    void setUp() {
        // Clear the user database
        userDBRepository.deleteAll();

        // Add a sample user
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .active(true)
                .build();

        userDBRepository.save(user);
    }

    @Test
    void registerUser_ShouldCreateNewUser() throws Exception {
        String newUserJson = """
                {
                    "username": "newuser",
                    "password": "newpassword",
                    "role": "USER"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        Optional<UserEntity> savedUser = userDBRepository.findByUsername("newuser");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getUsername()).isEqualTo("newuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_ShouldReturnCurrentUserDetails() throws Exception {
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(username = "nonexistentuser")
    void getCurrentUser_ShouldReturnUnauthorizedForNonexistentUser() throws Exception {
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }
}
