package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.domain.ReservationStatus;

public class UserReservationsResponse {

    private Long id;
    private String theme;
    private LocalDate date;
    private LocalTime time;
    private String status;

    private UserReservationsResponse() {
    }

    public UserReservationsResponse(final ReservationWithRank reservationWithRank) {
        Reservation reservation = reservationWithRank.reservation();
        this.id = reservation.getId();
        this.theme = reservation.getTheme().getName();
        this.date = reservation.getDate();
        this.time = reservation.getReservationTime().getStartAt();
        this.status = setReservedStatus(reservationWithRank);
    }

    private String setReservedStatus(ReservationWithRank reservationWithRank) {
        if (reservationWithRank.reservation().getStatus().equals(ReservationStatus.RESERVED)) {
            return reservationWithRank.reservation().getStatus().getStatus();
        }
        return reservationWithRank.rank() + "번째 예약대기";
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
