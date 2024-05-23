package roomescape.exception.reservation;

import java.time.LocalDate;

public class AlreadyBookedException extends ReservationException {
    private final LocalDate date;
    private final long timeId;
    private final long themeId;

    public AlreadyBookedException(LocalDate date, long timeId, long themeId) {
        super("이미 존재하는 예약입니다.");
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
