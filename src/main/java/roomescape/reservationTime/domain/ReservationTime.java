package roomescape.reservationTime.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    private ReservationTime(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime() {

    }

    public static ReservationTime createWithoutId(final LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime createWithId(final Long id, final LocalTime startAt) {
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    public ReservationTime assignId(final Long id) {
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    public boolean isSameTime(final ReservationTime time) {
        return startAt.equals(time.startAt);
    }

    public boolean isBeforeTime(final LocalTime time) {
        return startAt.isBefore(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
