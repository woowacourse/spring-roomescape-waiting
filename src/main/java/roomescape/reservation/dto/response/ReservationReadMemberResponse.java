package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;

public record ReservationReadMemberResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationReadMemberResponse from(Reservation reservation) {
        return new ReservationReadMemberResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                // TODO: 3단계에서 구현
                "예약"
        );
    }
}
