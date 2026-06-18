package roomescape.domain.reservationslot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationSlotErrors;
import roomescape.support.exception.errors.ReservationTimeErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"date_id", "time_id", "theme_id"})
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "date_id", nullable = false)
    private ReservationDate date;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private ReservationSlot(
        Long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        validate(date, time, theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot createWithoutId(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new ReservationSlot(
            null,
            date,
            time,
            theme
        );
    }

    public static ReservationSlot createWithId(long id, ReservationSlot reservation) {
        return of(
            id,
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );
    }

    public static ReservationSlot of(
        long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new ReservationSlot(
            id,
            date,
            time,
            theme
        );
    }

    private static void validate(ReservationDate date, ReservationTime time, Theme theme) {
        if (date == null) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_RESERVATION_DATE);
        }
        if (time == null) {
            throw new BadRequestException(ReservationTimeErrors.INVALID_RESERVATION_TIME);
        }
        if (theme == null) {
            throw new BadRequestException(ThemeErrors.INVALID_THEME);
        }
    }
}
