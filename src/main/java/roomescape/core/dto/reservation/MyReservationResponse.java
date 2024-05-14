package roomescape.core.dto.reservation;

import roomescape.core.domain.Reservation;

public class MyReservationResponse {
    private Long reservationId;
    private String theme;
    private String date;
    private String time;
    private String status;

    public MyReservationResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName(), reservation.getDateString(),
                reservation.getReservationTime().getStartAtString(), reservation.getStatus().getValue());
    }

    public MyReservationResponse(Long reservationId, String theme, String date, String time, String status) {
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
        return status;
    }
}
