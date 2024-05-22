package roomescape.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationRankStatusResponse {

    private final long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;
    private final long waitingRank;

    public ReservationRankStatusResponse(long reservationId,
                                         String theme,
                                         LocalDate date,
                                         LocalTime time,
                                         ReservationStatus status,
                                         long waitingRank) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status.toString();
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

    public String getStatus() {
        return status;
    }

    public long getWaitingRank() {
        return waitingRank;
    }
}
