package roomescape.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue(value = "confirm")
public class ConfirmedReservation extends Reservation{
    public ConfirmedReservation() {
    }

    public ConfirmedReservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme){
        super(id, member, date, time, theme);
    }

    public ConfirmedReservation(Member member, LocalDate date, ReservationTime time, Theme theme){
        super(null, member, date, time, theme);
    }

}
