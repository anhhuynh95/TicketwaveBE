package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketDBRepository extends JpaRepository<TicketEntity, Integer> {
}
