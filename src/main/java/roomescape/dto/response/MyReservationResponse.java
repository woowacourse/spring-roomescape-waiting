package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        CreateReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Integer order
) {
    public static MyReservationResponse fromReservation(Reservation reservation) {
        ReservationStatus status = reservation.getReservationStatus() == roomescape.domain.ReservationStatus.CONFIRMED
                ? ReservationStatus.RESERVED
                : ReservationStatus.PENDING_PAYMENT;
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                new CreateReservationTimeResponse(
                        reservation.getTime().getId(),
                        reservation.getTime().getStartAt()
                ),
                new ThemeResponse(
                        reservation.getTheme().getId(),
                        reservation.getTheme().getName(),
                        reservation.getTheme().getDescription(),
                        reservation.getTheme().getThumbnail()
                ),
                status,
                null
        );
    }

    public static MyReservationResponse fromReservationWaiting(ReservationWaitingOrderResponse waitingOrderResponse) {
        ReservationWaiting waiting = waitingOrderResponse.waiting();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getReservationDate(),
                new CreateReservationTimeResponse(
                        waiting.getTime().getId(),
                        waiting.getTime().getStartAt()
                ),
                new ThemeResponse(
                        waiting.getTheme().getId(),
                        waiting.getTheme().getName(),
                        waiting.getTheme().getDescription(),
                        waiting.getTheme().getThumbnail()
                ),
                ReservationStatus.WAITING,
                waitingOrderResponse.order()
        );
    }
}
