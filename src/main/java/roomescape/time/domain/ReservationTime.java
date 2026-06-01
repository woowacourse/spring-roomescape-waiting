package roomescape.time.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.common.exception.InactiveException;
import roomescape.common.exception.ValidationException;

@Getter
@EqualsAndHashCode
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final boolean isActive;

    private ReservationTime(Long id, LocalTime startAt, boolean isActive) {
        this.id = id;
        this.startAt = startAt;
        this.isActive = isActive;
    }

    public static ReservationTime create(LocalTime startAt) {
        validateStartAt(startAt);
        return new ReservationTime(null, startAt, true);
    }

    public static ReservationTime restore(Long id, LocalTime startAt, boolean isActive) {
        return new ReservationTime(id, startAt, isActive);
    }

    public ReservationTime deactivate() {
        return restore(id, startAt, false);
    }

    private static void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new ValidationException("시간은 필수 값입니다.");
        }
    }

    public void validateInactive() {
        if (!isActive()) {
            throw new InactiveException("비활성화 된 시간대입니다.");
        }
    }

    public LocalDateTime convertToDateTime(LocalDate date) {
        return LocalDateTime.of(date, startAt);
    }

    public boolean isAvailableAt(LocalDate date, Clock clock) {
        return !convertToDateTime(date).isBefore(LocalDateTime.now(clock));
    }
}
