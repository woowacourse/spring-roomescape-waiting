package roomescape.domain.reservationtime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationTimeErrors;

@Table(name = "reservation_time")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private ReservationTime(LocalTime startAt) {
        validate(startAt);
        this.id = null;
        this.startAt = startAt;
    }

    public static ReservationTime createWithoutId(LocalTime startAt) {
        return new ReservationTime(startAt);
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    private static void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException(ReservationTimeErrors.INVALID_RESERVATION_TIME);
        }
    }

    public boolean isBefore(LocalTime compareTime) {
        return startAt.isBefore(compareTime);
    }
}
