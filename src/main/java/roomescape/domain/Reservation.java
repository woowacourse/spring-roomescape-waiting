package roomescape.domain;

import jakarta.persistence.Entity;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation extends Booking {

    protected Reservation() {
    }

    private Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        super(id, member, date, time, theme);
    }

    public static Reservation create(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, member, date, time, theme);
    }

    public long calculateMinutesUntilStart(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(getDate(), getTime().getStartAt());
        return Duration.between(now, reservationDateTime).toMinutes();
    }
}
