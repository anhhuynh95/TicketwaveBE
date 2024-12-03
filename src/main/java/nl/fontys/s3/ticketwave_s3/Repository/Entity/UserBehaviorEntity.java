package nl.fontys.s3.ticketwave_s3.Repository.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_behavior")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    @Builder.Default
    private int warnings = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isBanned = false;
}

