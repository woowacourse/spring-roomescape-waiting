package roomescape.dto.reservation;

import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;

public class MyReservationResponse {
    private final Long id;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final ReservationStatus status;
    private final Long sequence;

    public MyReservationResponse(Long id, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, ReservationStatus status, Long sequence) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.sequence = sequence;
    }

    public static MyReservationResponse fromReservation(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static MyReservationResponse fromWaiting(ReservationWaiting reservationWaiting) {
        return new MyReservationResponse(
                reservationWaiting.getId(),
                reservationWaiting.getDate(),
                ReservationTimeResponse.from(reservationWaiting.getTime()),
                ThemeResponse.from(reservationWaiting.getTheme()),
                ReservationStatus.WAITING,
                reservationWaiting.getSequence()
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getSequence() {
        return sequence;
    }
}
