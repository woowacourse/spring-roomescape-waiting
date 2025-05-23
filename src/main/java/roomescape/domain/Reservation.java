package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private BookingInfo bookingInfo;

    protected Reservation() {
    }

    private Reservation(Long id, BookingInfo bookingInfo) {
        this.id = id;
        this.bookingInfo = bookingInfo;
    }

    public static Reservation create(BookingInfo bookingInfo) {
        return new Reservation(null, bookingInfo);
    }

    public long calculateMinutesUntilStart(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(getDate(), getTime().getStartAt());
        return Duration.between(now, reservationDateTime).toMinutes();
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
