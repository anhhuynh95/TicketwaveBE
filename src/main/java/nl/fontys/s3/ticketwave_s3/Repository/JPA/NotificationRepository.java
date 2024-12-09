package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.NotificationEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

    List<NotificationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    @Query("SELECT COUNT(n) > 0 FROM NotificationEntity n " +
            "WHERE n.message = :message " +
            "AND (:userId IS NULL AND n.user IS NULL OR n.user.id = :userId) " +
            "AND n.createdAt >= :recentTimestamp")
    boolean existsByMessageUserAndRecentTimestamp(@Param("message") String message,
                                                  @Param("userId") Integer userId,
                                                  @Param("recentTimestamp") LocalDateTime recentTimestamp);

    @Query("SELECT n FROM NotificationEntity n WHERE n.user IS NULL OR n.message LIKE 'Toxic comment%' ORDER BY n.createdAt DESC")
    List<NotificationEntity> findAdminNotifications();

}
