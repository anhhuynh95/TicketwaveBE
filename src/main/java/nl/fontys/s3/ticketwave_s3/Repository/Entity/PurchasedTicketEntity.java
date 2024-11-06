package nl.fontys.s3.ticketwave_s3.Repository.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchased_ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchasedTicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private TicketEntity ticket;

    @NotNull
    @Column(name = "purchase_quantity")
    private Integer purchaseQuantity;

    @NotNull
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Override
    public String toString() {
        return "PurchasedTicketEntity{id=" + id + ", purchaseQuantity=" + purchaseQuantity + ", purchaseDate=" + purchaseDate + "}";
    }
}
