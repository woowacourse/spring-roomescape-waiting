package roomescape.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public record MemberReservationResponse(Long reservationId,
                                        String theme,
                                        LocalDate date,
                                        LocalTime time,
                                        String status) {

    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public static MemberReservationResponse from(Waiting waiting) {
        return new MemberReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                "대기"
        );
    }
}
