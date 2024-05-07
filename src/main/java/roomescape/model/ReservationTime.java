package roomescape.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private LocalTime startAt;
    @OneToMany(mappedBy = "time")
    private Set<Reservation> reservations;

    public ReservationTime(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(0, startAt);
    }

    public ReservationTime() {
    }

    public static ReservationTime from(ReservationTimeDto reservationTimeDto) {
        return new ReservationTime(reservationTimeDto.getStartAt());
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationTime that = (ReservationTime) o;
        return id == that.id && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
