package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MemberReservationCreateRequest(
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다.") LocalDate date,
        @NotNull(message = "예약 시간은 비어있을 수 없습니다.") Long timeId,
        @NotNull(message = "테마는 비어있을 수 없습니다.") Long themeId
) {

    public static MemberReservationCreateRequest from(ReservationCreateRequest request) {
        return new MemberReservationCreateRequest(request.date(), request.timeId(), request.themeId());
    }
}
