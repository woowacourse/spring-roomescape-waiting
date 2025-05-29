package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import roomescape.exception.BadRequestException;

@Entity
@Table(name = "reservation_time")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException("startAt이 null 입니다.");
        }
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime() {
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isSameReservationTime(final ReservationTime reservationTime) {
        return id.equals(reservationTime.getId());
    }

    public boolean isPast(LocalTime target) {
        return startAt.isBefore(target);
    }
}
