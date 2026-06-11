package roomescape.service.event;

import java.time.LocalDate;

public class ReservationCancelledEvent {
    private final Long reservationId;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public ReservationCancelledEvent(Long reservationId, LocalDate date, Long timeId, Long themeId) {
        this.reservationId = reservationId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
