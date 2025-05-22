package roomescape.domain;

import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class Waiting extends Booking {

    protected Waiting() {
    }

    private Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        super(id, member, date, time, theme);
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
}
