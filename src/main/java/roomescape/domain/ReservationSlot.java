package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationSlot {
    private final LocalDate date;
    private final long timeId;
    private final long themeId;

    public ReservationSlot(LocalDate date, long timeId, long themeId) {
        Objects.requireNonNull(date, "예약 날짜는 필수값 입니다.");
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }

    public long getThemeId() {
        return themeId;
    }
}
