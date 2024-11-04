package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketDBRepository extends JpaRepository<TicketEntity, Integer> {
    List<TicketEntity> findByEventId(Integer eventId);
}
