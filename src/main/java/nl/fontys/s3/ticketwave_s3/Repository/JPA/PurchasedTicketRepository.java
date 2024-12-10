package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchasedTicketRepository extends JpaRepository<PurchasedTicketEntity, Integer> {
    List<PurchasedTicketEntity> findByTicketId(Integer ticketId);
    List<PurchasedTicketEntity> findByUserId(Integer userId);

    @Query("SELECT e.eventType, SUM(p.purchaseQuantity) " +
            "FROM PurchasedTicketEntity p " +
            "JOIN p.ticket t " +
            "JOIN t.event e " +
            "WHERE p.purchaseDate >= :startDate " +
            "GROUP BY e.eventType")
    List<Object[]> findPurchasesByEventType(@Param("startDate") LocalDateTime startDate);


    @Query("SELECT MONTH(p.purchaseDate) AS month, SUM(p.purchaseQuantity * t.price) AS totalSales " +
            "FROM PurchasedTicketEntity p " +
            "JOIN p.ticket t " +
            "WHERE p.purchaseDate >= :startDate " +
            "GROUP BY MONTH(p.purchaseDate) " +
            "ORDER BY month")
    List<Object[]> findMonthlySales(@Param("startDate") LocalDateTime startDate);


}
