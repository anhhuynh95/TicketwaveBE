package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.transaction.Transactional;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.EventDBRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentDBRepository commentDBRepository;

    @Autowired
    private EventDBRepository eventDBRepository;

    @Autowired
    private UserDBRepository userDBRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clear all repositories
        commentDBRepository.deleteAll();
        eventDBRepository.deleteAll();
        userDBRepository.deleteAll();

        // Add a sample user
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .active(true)
                .build();
        userDBRepository.save(user);

        // Add a sample event
        EventEntity event = EventEntity.builder()
                .name("Music Festival")
                .location("Amsterdam")
                .description("A grand music festival")
                .dateTime("2025-01-15T18:00:00")
                .ticketQuantity(100)
                .eventType(EventType.MUSIC)
                .build();
        eventDBRepository.save(event);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void addComment_ShouldCreateComment() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        String newCommentJson = """
                {
                    "eventId": %d,
                    "commentText": "This is a test comment"
                }
                """.formatted(eventId);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCommentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentText").value("This is a test comment"))
                .andExpect(jsonPath("$.eventId").value(eventId));

        assertThat(commentDBRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void addComment_ShouldReturnBadRequestForEmptyText() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        String newCommentJson = """
                {
                    "eventId": %d,
                    "commentText": ""
                }
                """.formatted(eventId);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCommentJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Comment text cannot be empty."));

        assertThat(commentDBRepository.findAll()).isEmpty();
    }

    @Test
    void getCommentsByEventId_ShouldReturnAllCommentsForEvent() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        // Add a sample comment
        CommentEntity comment = CommentEntity.builder()
                .eventId(eventId)
                .userId(userDBRepository.findAll().get(0).getId())
                .commentText("Sample comment")
                .build();
        commentDBRepository.save(comment);

        mockMvc.perform(get("/comments/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].commentText").value("Sample comment"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteComment_ShouldDeleteComment() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        // Add a sample comment
        CommentEntity comment = CommentEntity.builder()
                .eventId(eventId)
                .userId(userDBRepository.findAll().get(0).getId())
                .commentText("Sample comment")
                .build();
        CommentEntity savedComment = commentDBRepository.save(comment);

        mockMvc.perform(delete("/comments/{commentId}", savedComment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted successfully."));

        assertThat(commentDBRepository.findById(savedComment.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteComment_ShouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(delete("/comments/{commentId}", 999))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Error: Comment not found."));
    }
}
