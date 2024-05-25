package roomescape.service.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationwaiting.ReservationWaitingWithRank;

public class ReservationMineResponse {
    private final Long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public ReservationMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public ReservationMineResponse(Reservation reservation) {
        this(reservation.getId(),
                reservation.getTheme().getName().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                ReservationStatus.BOOKED.getDescription()
        );
    }

    public ReservationMineResponse(ReservationWaitingWithRank waitingWithRank) {
        this(waitingWithRank.getWaiting().getReservation().getId(),
                waitingWithRank.getWaiting().getReservation().getTheme().getName().getName(),
                waitingWithRank.getWaiting().getReservation().getDate(),
                waitingWithRank.getWaiting().getReservation().getTime().getStartAt(),
                String.format(ReservationStatus.WAITING.getDescription(), waitingWithRank.getRank())
        );
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time);
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
