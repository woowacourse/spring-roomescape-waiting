package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationSlot {
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(date, "예약 날짜는 필수값 입니다.");
        Objects.requireNonNull(time, "예약 시간은 필수값 입니다.");
        Objects.requireNonNull(theme, "테마는 필수값 입니다.");
        this.date = date;
        this.time = time;
        this.theme = theme;
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
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReservationSlot slot = (ReservationSlot) object;
        return Objects.equals(date, slot.date)
                && Objects.equals(time, slot.time)
                && Objects.equals(theme, slot.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
