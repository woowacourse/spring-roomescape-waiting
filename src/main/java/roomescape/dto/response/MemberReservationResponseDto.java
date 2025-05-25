package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.WaitingWithRank;

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

    public static MemberReservationResponseDto from(final WaitingWithRank waitingWithRank) {
        return new MemberReservationResponseDto(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getTheme().getName(),
                waitingWithRank.waiting().getDate(),
                waitingWithRank.waiting().getReservationTime().getStartAt(),
                waitingWithRank.rank() + 1 + "번째 예약대기"
        );
    }
}
