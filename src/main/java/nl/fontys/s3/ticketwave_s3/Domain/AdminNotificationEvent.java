package nl.fontys.s3.ticketwave_s3.Domain;

import lombok.Getter;

public class AdminNotificationEvent {
    private final String username;
    private final String commentText;
    @Getter
    private final Integer userId;
    @Getter
    private final Integer commentId;
    @Getter
    private final Integer eventId;

    public AdminNotificationEvent(String username, String commentText, Integer userId, Integer commentId, Integer eventId) {
        this.username = username;
        this.commentText = commentText;
        this.userId = userId;
        this.commentId = commentId;
        this.eventId = eventId;
    }

    public String getMessage() {
        return "Toxic comment detected from User '" + username + "': '" + commentText + "'. Admin action required.";
    }

}
