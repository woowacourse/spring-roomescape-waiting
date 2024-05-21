package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public class MyReservationResponse {

    static private final String STATUS = "예약";

    private final Long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;

    public MyReservationResponse(final Long reservationId, final String theme, final LocalDate date,
            final LocalTime time) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
    }

    public MyReservationResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName(), reservation.getDate(),
                reservation.getTime().getStartAt());
    }

    public Long getReservationId() {
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
        return STATUS;
    }
}
