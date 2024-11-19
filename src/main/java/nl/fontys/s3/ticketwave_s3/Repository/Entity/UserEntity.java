package nl.fontys.s3.ticketwave_s3.Repository.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(unique = true)
    private String username;

    @NotNull
    @Column
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    private UserRole role;

    @NotNull
    @Column
    private boolean active = true;

    @NotNull
    @Column
    private LocalDateTime createdAt = LocalDateTime.now();
}
