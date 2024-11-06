package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasedTicketRepository extends JpaRepository<PurchasedTicketEntity, Integer> {
    List<PurchasedTicketEntity> findByTicketId(Integer ticketId);
}
