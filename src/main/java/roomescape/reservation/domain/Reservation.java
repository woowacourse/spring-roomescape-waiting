package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ValidationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Getter
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

    public static Reservation create(String name, LocalDate date, ReservationTime time, Theme theme, Status status,
                                     Clock clock) {
        validateRequiredFields(name, date, time, theme, clock);
        return new Reservation(null, name, date, time, theme, status, LocalDateTime.now(clock));
    }

    public static Reservation restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
                                      Status status, LocalDateTime createdAt) {
        return new Reservation(id, name, date, time, theme, status, createdAt);
    }

    public Reservation modify(LocalDate date, ReservationTime time, Theme theme, Status status, Clock clock) {
        validateRequiredFields(name, date, time, theme, clock);
        validateModifiable(clock);

        return restore(this.id, this.name, date, time, theme, status, createdAt);
    }

    public boolean isOwner(String username) {
        return this.name.equals(username);
    }

    public Reservation cancel() {
        return restore(id, name, date, time, theme, Status.CANCELED, createdAt);
    }

    public Reservation reserved() {
        return restore(id, name, date, time, theme, Status.RESERVED, createdAt);
    }

    public boolean isReserved(){
        return this.status.equals(Status.RESERVED);
    }

    public boolean isWaiting() {
        return this.status.equals(Status.WAITING);
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
        theme.validateInactive();
    }

    private static void validateDateTime(LocalDate date, ReservationTime time, Clock clock) {
        if (date == null) {
            throw new ValidationException("날짜는 필수입니다.");
        }

        if (time == null) {
            throw new ValidationException("시간은 필수입니다.");
        }

        validateNotPast(clock, date, time);
        time.validateInactive();
    }

    private void validateModifiable(Clock clock) {
        if (status == Status.CANCELED) {
            throw new ConflictException("이미 취소된 예약은 변경할 수 없습니다.");
        }

        if(!time.isAvailableAt(date, clock)){
            throw new ConflictException("이미 지난 예약은 변경할 수 없습니다.");
        }
    }

    private static void validateNotPast(Clock clock, LocalDate date, ReservationTime time) {
        if (!time.isAvailableAt(date, clock)) {
            throw new ConflictException("과거 시간대로 예약할 수 없습니다.");
        }
    }
}
