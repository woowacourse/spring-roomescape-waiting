package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.common.exception.ValidationException;
import roomescape.reservation.domain.exception.IllegalReservationDateTimeException;
import roomescape.reservation.domain.exception.IllegalStateReservationException;
import roomescape.reservation.domain.exception.PastDateTimeException;
import roomescape.reservation.domain.exception.UnauthorizedReservationChangeException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Getter
@EqualsAndHashCode
public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Status status;
    private final LocalDateTime createdAt;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme, Status status,
                        LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation create(String name, LocalDate date, ReservationTime time, Theme theme, Clock clock) {
        validateRequiredFields(name, date, time, theme, clock);
        return new Reservation(null, name, date, time, theme, Status.ACTIVE, LocalDateTime.now(clock));
    }

    public static Reservation createWaiting(String name, LocalDate date, ReservationTime time, Theme theme,
                                            Clock clock) {
        validateRequiredFields(name, date, time, theme, clock);
        return new Reservation(null, name, date, time, theme, Status.WAITING, LocalDateTime.now(clock));
    }

    public static Reservation restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
                                      Status status, LocalDateTime createdAt) {
        return new Reservation(id, name, date, time, theme, status, createdAt);
    }

    public Reservation modify(LocalDate date, ReservationTime time, Theme theme, Status status, Clock clock) {
        validateModifiable(clock);

        return restore(this.id, this.name, date, time, theme, status, createdAt);
    }

    public void validateOwner(String username) {
        if (!this.name.equals(username)) {
            throw new UnauthorizedReservationChangeException("예약 변경 권한이 없습니다.");
        }
    }

    public Reservation cancel() {
        return restore(id, name, date, time, theme, Status.CANCELED, createdAt);
    }

    public Reservation active() {
        return restore(id, name, date, time, theme, Status.ACTIVE, createdAt);
    }

    private static void validateRequiredFields(String name, LocalDate date, ReservationTime time, Theme theme,
                                               Clock clock) {
        validateName(name);
        validateTheme(theme);
        validateDateTime(date, time, clock);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("예약자 이름은 필수입니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new ValidationException("테마는 필수입니다.");
        }
    }

    private static void validateDateTime(LocalDate date, ReservationTime time, Clock clock) {
        if (date == null) {
            throw new ValidationException("날짜는 필수입니다.");
        }

        if (time == null) {
            throw new ValidationException("시간은 필수입니다.");
        }

        if (!time.isAvailableAt(date, clock)) {
            throw new PastDateTimeException("현재보다 이전 시간대로 예약할 수 없습니다.");
        }
    }

    private void validateModifiable(Clock clock) {
        if (status == Status.CANCELED) {
            throw new IllegalStateReservationException("이미 취소된 예약은 변경할 수 없습니다.");
        }

        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now(clock))) {
            throw new IllegalReservationDateTimeException("이미 지난 예약은 변경할 수 없습니다.");
        }
    }
}
