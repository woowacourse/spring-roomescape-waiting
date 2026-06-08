package roomescape.domain.reservation;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.DomainPreconditions;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.requireNonNull;

public class Slot {
    private final Long id;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Slot(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = requireNonNull(date, INVALID_INPUT, "예약일은 비어있을 수 없습니다.");
        this.time = requireNonNull(time, INVALID_INPUT, "예약 시간은 비어있을 수 없습니다.");
        this.theme = requireNonNull(theme, INVALID_INPUT, "예약 테마는 비어있을 수 없습니다.");
    }

    public static Slot load(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, new ReservationDate(date), time, theme);
    }

    public static Slot create(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        Slot slot = new Slot(null, date, time, theme);
        slot.validateNotPast(now);
        return slot;
    }

    public void validateNotPast(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(DomainErrorCode.PAST_DATE, date.getDate(), time.getStartAt());
        }
    }

    public Slot withId(Long generatedKey) {
        return new Slot(generatedKey, date, time, theme);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date.getDate(), time.getStartAt()).isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
