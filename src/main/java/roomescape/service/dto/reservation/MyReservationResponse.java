package roomescape.service.dto.reservation;

import roomescape.domain.reservation.Reservation;

public class MyReservationResponse {

    private static final String DEFAULT_STATUS = "예약";

    private final long reservationId;
    private final String theme;
    private final String date;
    private final String time;
    private final String status;

    public MyReservationResponse(long reservationId, String theme, String date, String time) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = DEFAULT_STATUS;
    }

    public MyReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.themeName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString());
    }

    public long getReservationId() {
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
