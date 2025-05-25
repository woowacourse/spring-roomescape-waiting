package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedDateTime;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @OneToOne(mappedBy = "waiting")
    private Reservation reservation;

    private Waiting(Long id, LocalDateTime savedDateTime, ReservationStatus status) {
        this.id = id;
        this.savedDateTime = savedDateTime;
        this.status = status;
    }

    protected Waiting() {
    }

    private Waiting(ReservationStatus status) {
        this(null, null, status);
    }

    private Waiting(LocalDateTime savedDateTime, ReservationStatus status) {
        this(null, savedDateTime, status);
    }

    public static Waiting waitingWithoutId(ReservationStatus status) {
        return new Waiting(status);
    }

    public static Waiting waitingWithoutId(LocalDateTime dateTime, ReservationStatus status) {
        return new Waiting(dateTime, status);
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

    public void setStatus(ReservationStatus reservationStatus) {
        this.status = reservationStatus;
    }
}
