package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private BookingInfo bookingInfo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime waitingStartedAt;

    protected Waiting() {
    }

    private Waiting(Long id, BookingInfo bookingInfo) {
        this.id = id;
        this.bookingInfo = bookingInfo;
    }

    public static Waiting create(BookingInfo bookingInfo) {
        return new Waiting(null, bookingInfo);
    }

    public Reservation confirm() {
        return Reservation.create(this.bookingInfo);
    }

    public boolean isOwnedBy(Long memberId) {
        return getMember().getId().equals(memberId);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return bookingInfo.getMember();
    }

    public LocalDate getDate() {
        return bookingInfo.getDate();
    }

    public ReservationTime getTime() {
        return bookingInfo.getTime();
    }

    public Theme getTheme() {
        return bookingInfo.getTheme();
    }

    public boolean isPast(Clock clock) {
        return bookingInfo.isPast(clock);
    }
}
