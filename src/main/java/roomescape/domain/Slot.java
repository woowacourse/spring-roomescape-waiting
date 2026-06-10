package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.custom.InvalidDomainValueException;

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

    public boolean isPast(LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (date.isBefore(nowDate)) {
            return true;
        }
        if (date.isAfter(nowDate)) {
            return false;
        }
        return time.isPast(nowTime);
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

    private void validate(LocalDate date, ReservationTime time, Theme theme) {
        if (date == null) {
            throw new InvalidDomainValueException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new InvalidDomainValueException("예약 시간은 비어 있을 수 없습니다.");
        }
        if (theme == null) {
            throw new InvalidDomainValueException("테마는 비어 있을 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Slot slot = (Slot) object;
        return Objects.equals(date, slot.date) && Objects.equals(time, slot.time)
                && Objects.equals(theme, slot.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
