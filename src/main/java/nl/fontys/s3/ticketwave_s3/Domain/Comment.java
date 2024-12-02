package nl.fontys.s3.ticketwave_s3.Domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    private Integer id;
    private Integer eventId;
    private Integer userId;
    private String commentText;
    private LocalDateTime createdAt;
}
