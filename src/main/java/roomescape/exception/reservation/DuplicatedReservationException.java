package roomescape.exception.reservation;

import java.time.LocalDate;

public class DuplicatedReservationException extends ReservationException {
    private final long themeId;
    private final LocalDate date;
    private final long timeId;

    public DuplicatedReservationException(long themeId, LocalDate date, long timeId) {
        super("이미 예약했거나 대기한 항목입니다.");
        this.themeId = themeId;
        this.date = date;
        this.timeId = timeId;
    }

    public long getThemeId() {
        return themeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }
}
