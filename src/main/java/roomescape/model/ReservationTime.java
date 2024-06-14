package roomescape.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;
import roomescape.service.dto.ReservationTimeDto;

@Entity
public class ReservationTime {

    private static final int INITIAL_STATE_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private LocalTime startAt;
    @OneToMany(mappedBy = "time", fetch = FetchType.LAZY)
    private Set<Reservation> reservations;

    public ReservationTime(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(0, startAt);
    }

    public ReservationTime(ReservationTimeDto reservationTimeDto) {
        this(INITIAL_STATE_ID, reservationTimeDto.getStartAt());
    }

    public ReservationTime() {
    }

    public long getId() {
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime reservationTime = (ReservationTime) o;
        return id == reservationTime.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
