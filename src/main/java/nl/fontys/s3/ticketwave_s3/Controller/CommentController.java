package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.CommentService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Service.ModerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody @Valid CommentDTO commentDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to log in to comment.");
        }

        // Extract username from Authentication principal
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Fetch the User from the database using the username
        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        User user = optionalUser.get();

        // Set userId and username in CommentDTO
        commentDTO.setUserId(user.getId());
        commentDTO.setUsername(user.getUsername());

        if (commentDTO.getEventId() == null || commentDTO.getCommentText() == null || commentDTO.getCommentText().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Comment text cannot be empty.");
        }

        try {
            CommentDTO createdComment = commentService.addComment(commentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Error adding comment: " + ex.getMessage());
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByEventId(@PathVariable Integer eventId) {
        List<CommentDTO> comments = commentService.getCommentsByEventId(eventId);
        return ResponseEntity.ok(comments);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Integer commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok("Comment deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }
}
