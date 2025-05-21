package roomescape.entity;

import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class Waiting extends Reservation{

    public Waiting() {}

    public Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        super(id, member, date, time, theme);
    }
    public Waiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }
}
