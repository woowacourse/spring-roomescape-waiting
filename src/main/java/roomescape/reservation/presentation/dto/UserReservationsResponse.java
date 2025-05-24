package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public class UserReservationsResponse {

    private Long id;
    private String theme;
    private LocalDate date;
    private LocalTime time;
    private String status;

    private UserReservationsResponse() {
    }

    public UserReservationsResponse(final Reservation reservation) {
        this.id = reservation.getId();
        this.theme = reservation.getReservationInfo().getTheme().getName();
        this.date = reservation.getReservationInfo().getDate();
        this.time = reservation.getReservationInfo().getReservationTime().getStartAt();
        this.status = "예약";
    }

    public UserReservationsResponse(final WaitingWithRank reservation) {
        Waiting waiting = reservation.getWaiting();
        this.id = waiting.getId();
        this.theme = waiting.getReservationInfo().getTheme().getName();
        this.date = waiting.getReservationInfo().getDate();
        this.time = waiting.getReservationInfo().getReservationTime().getStartAt();
        this.status = reservation.getRank() + "번째 예약대기";
    }

    public Long getId() {
        return id;
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
