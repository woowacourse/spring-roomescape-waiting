package roomescape.domain.waitingreservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "date_id", "time_id", "theme_id"})
})
@Getter
public class WaitingReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_id")
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    private LocalDateTime createdAt;

    protected WaitingReservation() {

    }

    private WaitingReservation(Long id, String name, ReservationDate date, ReservationTime time, Theme theme,
        LocalDateTime createdAt) {
        validate(name, createdAt);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public static WaitingReservation createWithoutId(String name, ReservationDate date, ReservationTime time,
        Theme theme, LocalDateTime createdAt) {
        return new WaitingReservation(null, name, date, time, theme, createdAt);
    }

    public static WaitingReservation of(Long id, String name, ReservationDate date, ReservationTime time, Theme theme,
        LocalDateTime createdAt) {
        return new WaitingReservation(id, name, date, time, theme, createdAt);
    }

    private static void validate(String name, LocalDateTime createdAt) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_RESERVATION_NAME);
        }

        if (createdAt == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_CREATED_AT);
        }
    }
}
