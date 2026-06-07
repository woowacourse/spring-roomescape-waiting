package roomescape.domain.reservation;

import roomescape.common.exception.UnprocessableException;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Slot {
    private final Long id;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Slot(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;

    }

    public static Slot load(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, new ReservationDate(date), time, theme);
    }

    public static Slot create(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        Slot slot = new Slot(null, date, time, theme);
        slot.ensureNotPast(now);
        return slot;
    }

    public Slot withId(long generatedKey) {
        return new Slot(generatedKey, date, time, theme);
    }

    private void ensureNotPast(LocalDateTime now) {
        if (LocalDateTime.of(date.getDate(), time.getStartAt()).isBefore(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }
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
