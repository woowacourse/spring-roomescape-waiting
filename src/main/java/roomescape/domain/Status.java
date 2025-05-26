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
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedDateTime;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @OneToOne(mappedBy = "status")
    private Reservation reservation;

    private Status(Long id, LocalDateTime savedDateTime, ReservationStatus status) {
        this.id = id;
        this.savedDateTime = savedDateTime;
        this.status = status;
    }

    protected Status() {
    }

    private Status(ReservationStatus status) {
        this(null, null, status);
    }

    private Status(LocalDateTime savedDateTime, ReservationStatus status) {
        this(null, savedDateTime, status);
    }

    public static Status statusWithoutId(ReservationStatus status) {
        return new Status(status);
    }

    public static Status statusWithoutId(LocalDateTime dateTime, ReservationStatus status) {
        return new Status(dateTime, status);
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

    public void cancelStatus() {
        this.status = ReservationStatus.CANCELED;
    }

    public void reserveStatus() {
        this.status = ReservationStatus.RESERVED;
    }
}
