package roomescape.reservation.dto;

import roomescape.member.dto.MemberResponse;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationWaitingResponse from(final ReservationWaitingWithOrderDto reservationWaitingWithOrderDto) {
        return new ReservationWaitingResponse(
                reservationWaitingWithOrderDto.id(),
                new MemberResponse(
                        reservationWaitingWithOrderDto.member().id(),
                        reservationWaitingWithOrderDto.member().name().getValue(),
                        reservationWaitingWithOrderDto.member().email().getValue()
                ),
                reservationWaitingWithOrderDto.date().getValue(),
                ReservationTimeResponse.from(reservationWaitingWithOrderDto.time()),
                ThemeResponse.from(reservationWaitingWithOrderDto.theme())
        );
    }
}
