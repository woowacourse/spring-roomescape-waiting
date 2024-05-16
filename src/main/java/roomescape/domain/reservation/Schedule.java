package roomescape.domain.reservation;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Schedule {
    private static final long NO_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "DATE"))
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    protected Schedule() {
    }

    public Schedule(long id, ReservationDate date, ReservationTime time) {
        this.id = id;
        this.date = date;
        this.time = time;
    }

    public Schedule(ReservationDate date, ReservationTime time) {
        this(NO_ID, date, time);
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public LocalTime getTime() {
        return time.getStartAt();
    }

    public ReservationTime getReservationTime() {
        return time;
    }
}
