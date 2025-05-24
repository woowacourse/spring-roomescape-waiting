package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWait;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationWithStatusResponse {

    private static final String CONFIRMED = "예약";
    private static final String PENDING_STATUS_FORMAT = "%d번째 예약대기";

    private final Long reservationId;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public static ReservationWithStatusResponse from(
            final Reservation reservation
    ) {
        return new ReservationWithStatusResponse(
                reservation.getId(),
                reservation.getTheme().getName().getValue(),
                reservation.getDate().getValue(),
                reservation.getTime().getStartAt(),
                CONFIRMED
        );
    }

    public static ReservationWithStatusResponse of(
            final ReservationWait reservationWait,
            final Long rank
    ) {
        return new ReservationWithStatusResponse(
                reservationWait.getId(),
                reservationWait.getTheme().getName().getValue(),
                reservationWait.getDate().getValue(),
                reservationWait.getTime().getStartAt(),
                String.format(PENDING_STATUS_FORMAT, rank)
        );
    }
}
