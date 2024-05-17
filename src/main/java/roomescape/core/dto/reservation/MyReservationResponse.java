package roomescape.core.dto.reservation;

import roomescape.core.domain.Reservation;
import roomescape.core.domain.Status;

public class MyReservationResponse {
    private final Long reservationId;
    private final String theme;
    private final String date;
    private final String time;
    private final Status status;

    public MyReservationResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName(), reservation.getDateString(),
                reservation.getReservationTime().getStartAtString(), reservation.getStatus());
    }

    public MyReservationResponse(Long reservationId, String theme, String date, String time, Status status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        if (status.equals(Status.BOOKED)) {
            return "예약";
        }
        return "예약 대기";
    }
}
