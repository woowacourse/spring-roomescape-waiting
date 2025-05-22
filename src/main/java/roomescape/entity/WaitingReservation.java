package roomescape.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue(value = "waiting")
public class WaitingReservation extends Reservation{

    public WaitingReservation() {
    }

    public WaitingReservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        super(id, member, date, time, theme);
    }
    public WaitingReservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }
}
