package roomescape.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record MemberReservationResponse(Long id,
                                        String theme,
                                        LocalDate date,
                                        LocalTime time,
                                        String status) {

    public static MemberReservationResponse from(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        String statusMessage = "예약";

        if (status.isWaiting()) {
            statusMessage = String.format("%d번째 예약대기", status.getPriority());
        }
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                statusMessage
        );
    }
}
