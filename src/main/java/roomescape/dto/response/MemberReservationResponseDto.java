package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.Reservation;
import roomescape.model.Waiting;

public record MemberReservationResponseDto(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MemberReservationResponseDto from(final Reservation reservation) {
        return new MemberReservationResponseDto(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                "예약"
        );
    }

    public static MemberReservationResponseDto from(final Waiting waiting) {
        return new MemberReservationResponseDto(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt(),
                "예약대기"
        );
    }
}
