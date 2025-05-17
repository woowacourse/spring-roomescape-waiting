package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public class UserReservationsResponse {

    private Long reservationId;
    private String theme;
    private LocalDate date;
    private LocalTime time;
    private String status;

    private UserReservationsResponse() {
    }

    public UserReservationsResponse(final Reservation reservation) {
        this.reservationId = reservation.getId();
        this.theme = reservation.getTheme().getName();
        this.date = reservation.getDate();
        this.time = reservation.getReservationTime().getStartAt();
        this.status = reservation.getStatus().getStatus();
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
        return status;
    }
}
