package nl.fontys.s3.ticketwave_s3.Repository.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;

import java.util.List;

@Entity
@Table(name = "event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "date_time")
    private String dateTime;

    @NotNull
    @Column(name = "ticket_quantity")
    private Integer ticketQuantity;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column
    private EventType eventType;
}
