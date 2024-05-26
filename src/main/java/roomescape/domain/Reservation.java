package roomescape.domain;

import jakarta.persistence.Entity;

@Entity
public class Reservation extends ReservationBase {

    protected Reservation() {
    }

    public Reservation(final Member member, final Schedule schedule) {
        super(member, schedule);
    }

    public boolean isSameTime(ReservationTime reservationTime) {
        return schedule.isSameTime(reservationTime);
    }
}
