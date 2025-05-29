package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MemberReservationResponseDto(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    private static final String RESERVATION_STATUS = "예약";

    public static MemberReservationResponseDto from(final Reservation reservation) {
        return new MemberReservationResponseDto(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                RESERVATION_STATUS
        );
    }

    public static MemberReservationResponseDto from(final WaitingWithRankDto waitingWithRankDto) {
        return new MemberReservationResponseDto(
                waitingWithRankDto.waiting().getId(),
                waitingWithRankDto.waiting().getTheme().getName(),
                waitingWithRankDto.waiting().getDate(),
                waitingWithRankDto.waiting().getReservationTime().getStartAt(),
                waitingWithRankDto.rank().toString()
        );
    }
}
