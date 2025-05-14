package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime savedDateTime;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @OneToOne(mappedBy = "waiting")
    private Reservation reservation;

    public Waiting() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getSavedDateTime() {
        return savedDateTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
