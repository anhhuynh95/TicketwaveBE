package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviorDTO {
    private Integer userId;
    private int warnings;
    private boolean isBanned;
}

