package roomescape.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationRankResponse {

    private final long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final long waitingRank;

    public ReservationRankResponse(long reservationId, String theme, LocalDate date, LocalTime time, long waitingRank) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.waitingRank = waitingRank;
    }

    public long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public long getWaitingRank() {
        return waitingRank;
    }
}
