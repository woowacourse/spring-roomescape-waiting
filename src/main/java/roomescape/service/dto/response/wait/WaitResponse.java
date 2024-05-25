package roomescape.service.dto.response.wait;

import static roomescape.domain.ReservationStatus.RESERVED;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.domain.ReservationWait;

public record WaitResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status) {

    public WaitResponse(ReservationWait wait, String statusText) {
        this(
                wait.getReservation().getId(),
                wait.getReservation().getTheme().getName(),
                wait.getReservation().getDate(),
                wait.getReservation().getTime().getStartAt(),
                statusText
        );
    }

    public static WaitResponse from(ReservationWait wait, long rank) {
        if (wait.getStatus().equals(RESERVED)) {
            return new WaitResponse(wait, "예약");
        }
        return new WaitResponse(wait, rank + "번째 예약대기");
    }
}
