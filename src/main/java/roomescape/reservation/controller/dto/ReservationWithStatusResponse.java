package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationWithStatusResponse {

    private static final String WAIT_STATUS_FORMAT = "%d번째 %s";

    private final Long reservationId;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public ReservationWithStatusResponse(
            final Long reservationId,
            final String themeName,
            final LocalDate date,
            final LocalTime time,
            final String status,
            final Long waitTime
    ) {
        this.reservationId = reservationId;
        this.themeName = themeName;
        this.date = date;
        this.time = time;
        this.status = String.format(WAIT_STATUS_FORMAT, waitTime, status);
    }
}
