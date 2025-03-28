package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Domain.AdminNotificationEvent;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.CommentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final RestTemplate restTemplate;
    private final CommentRepository commentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final String API_KEY = "AIzaSyDVQ7Ia5SxOM0A_zJzyWzcvKF7qI0M80qQ";
    private static final String API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";

    public void checkAndFlagComment(CommentDTO commentDTO) {
        boolean isToxic = isToxic(commentDTO.getCommentText());

        if (isToxic) {
            System.out.println("Comment flagged as toxic. Updating in the database.");

            CommentEntity comment = commentRepository.findById(commentDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found."));

            comment.setToxic(true);
            commentRepository.save(comment);

            messagingTemplate.convertAndSend(
                    "/topic/comment-updates/" + comment.getEventId(),
                    Map.of("flaggedCommentId", comment.getId(), "isToxic", true)
            );

            // Publish the event
            AdminNotificationEvent event = new AdminNotificationEvent(
                    commentDTO.getUsername(),
                    commentDTO.getCommentText(),
                    commentDTO.getUserId(),
                    commentDTO.getId(),
                    comment.getEventId()
            );
            eventPublisher.publishEvent(event);

            System.out.println("Notifying admins about toxic comment...");
        }
    }


    @SuppressWarnings("unchecked")
    private boolean isToxic(String commentText) {
        // Prepare payload and headers
        Map<String, Object> payload = Map.of(
                "comment", Map.of("text", commentText),
                "languages", List.of("en"),
                "requestedAttributes", Map.of("TOXICITY", Map.of())
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            // Send POST API request to Google
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL + API_KEY,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Parse the response
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            Map<String, Object> attributeScores = (Map<String, Object>) responseBody.get("attributeScores");
            Map<String, Object> toxicity = (Map<String, Object>) attributeScores.get("TOXICITY");
            Map<String, Object> summaryScore = (Map<String, Object>) toxicity.get("summaryScore");

            Double score = (Double) summaryScore.get("value");

            System.out.println("Toxicity Score: " + score);

            return score > 0.2; // Return true if the score exceeds 0.2
        } catch (Exception e) {
            throw new IllegalArgumentException("Error during toxicity check", e);
        }
    }
}
