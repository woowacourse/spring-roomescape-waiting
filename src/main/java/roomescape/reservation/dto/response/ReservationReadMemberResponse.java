package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.Waiting;

public record ReservationReadMemberResponse(
        Long id,
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
                "예약"
        );
    }

    public static ReservationReadMemberResponse from(Waiting waiting, int position) {
        return new ReservationReadMemberResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                String.format("%d번째 예약대기", position + 1)
        );
    }
}
