package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.exception.InvalidDomainException;

public class Slot {
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Slot(LocalDate date, ReservationTime time, Theme theme) {
        validate(date, time, theme);
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private static void validate(LocalDate date, ReservationTime time, Theme theme) {
        if (date == null) {
            throw new InvalidDomainException("날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new InvalidDomainException("시간은 비어 있을 수 없습니다.");
        }
        if (theme == null) {
            throw new InvalidDomainException("테마는 비어 있을 수 없습니다.");
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Slot slot = (Slot) o;
        return Objects.equals(date, slot.date) && Objects.equals(time, slot.time)
                && Objects.equals(theme, slot.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
