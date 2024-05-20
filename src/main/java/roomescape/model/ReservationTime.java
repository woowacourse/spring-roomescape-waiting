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
    private Long id;
    @NotNull
    private LocalTime startAt;
    @OneToMany(mappedBy = "time")
    private Set<Reservation> reservations;

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime() {
    }

    public static ReservationTime from(ReservationTimeDto reservationTimeDto) {
        return new ReservationTime(0L, reservationTimeDto.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
