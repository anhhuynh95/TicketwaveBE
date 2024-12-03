package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserBehaviorService;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.CommentRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final RestTemplate restTemplate;
    private final CommentRepository commentRepository;
    private final UserBehaviorService userBehaviorService;
    private final NotificationService notificationService;

    // Hardcoded API key
    private static final String API_KEY = "AIzaSyDVQ7Ia5SxOM0A_zJzyWzcvKF7qI0M80qQ";
    private static final String API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";

    public void checkAndFlagComment(CommentDTO commentDTO) {
        boolean isToxic = isToxic(commentDTO.getCommentText());

        if (isToxic) {

            System.out.println("Comment flagged as toxic. Updating in the database.");
            // Find the comment by ID
            CommentEntity comment = commentRepository.findById(commentDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found."));

            // Set the isToxic flag
            comment.setToxic(true);

            // Save the updated comment
            commentRepository.save(comment);

            // Warn the user
            userBehaviorService.warnUser(commentDTO.getUserId());

            // Notify admins
            UserBehaviorDTO behavior = userBehaviorService.getUserBehavior(commentDTO.getUserId());
            String message = behavior.getWarnings() == 1
                    ? String.format("User '%s' posted a toxic comment: '%s'", commentDTO.getUsername(), commentDTO.getCommentText())
                    : String.format("User '%s' posted another toxic comment. Consider banning this user.", commentDTO.getUsername());

            notificationService.notifyAdmins(message);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isToxic(String commentText) {
        Map<String, Object> payload = Map.of(
                "comment", Map.of("text", commentText),
                "languages", List.of("en"),
                "requestedAttributes", Map.of("TOXICITY", Map.of())
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL + API_KEY,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            Map<String, Object> attributeScores = (Map<String, Object>) responseBody.get("attributeScores");
            Map<String, Object> toxicity = (Map<String, Object>) attributeScores.get("TOXICITY");
            Map<String, Object> summaryScore = (Map<String, Object>) toxicity.get("summaryScore");

            Double score = (Double) summaryScore.get("value");

            // Log the response for debugging
            System.out.println("Toxicity Score: " + score);

            return score > 0.2;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error during toxicity check", e);
        }
    }
}
