package roomescape.application.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingStatus;

public record ReservationStatusResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static final String COUNT_WAITING = "번째 ";

    public static ReservationStatusResponse from(Reservation reservation) {
        Theme theme = reservation.getTheme();
        ReservationTime time = reservation.getTime();
        ReservationStatus status = reservation.getReservationStatus();
        return new ReservationStatusResponse(
                reservation.getId(),
                theme.getName(),
                reservation.getDate(),
                time.getStartAt(),
                status.getStatus()
        );
    }

    public static ReservationStatusResponse from(Waiting waiting) {
        Theme theme = waiting.getReservation().getTheme();
        ReservationTime time = waiting.getReservation().getTime();
        WaitingStatus status = waiting.getWaitingStatus();
        return new ReservationStatusResponse(
                waiting.getId(),
                theme.getName(),
                waiting.getReservation().getDate(),
                time.getStartAt(),
                status.getStatus()
        );
    }

    public static ReservationStatusResponse of(Waiting waiting, int index) {
        Theme theme = waiting.getReservation().getTheme();
        ReservationTime time = waiting.getReservation().getTime();
        WaitingStatus status = waiting.getWaitingStatus();
        return new ReservationStatusResponse(
                waiting.getId(),
                theme.getName(),
                waiting.getReservation().getDate(),
                time.getStartAt(),
                index + COUNT_WAITING + status.getStatus()
        );
    }
}
