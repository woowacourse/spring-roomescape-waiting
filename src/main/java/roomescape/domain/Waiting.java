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

    private Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.bookingInfo = new BookingInfo(member, date, time, theme);
    }

    public static Waiting create(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Waiting(null, member, date, time, theme);
    }

    public Reservation toReservation() {
        return Reservation.create(getMember(), getDate(), getTime(), getTheme());
    }

    public boolean sameWaiterWith(Long memberId) {
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
