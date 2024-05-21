package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;

public record MemberReservationCreateRequest(
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다.") LocalDate date,
        @NotNull(message = "예약 시간은 비어있을 수 없습니다.") Long timeId,
        @NotNull(message = "테마는 비어있을 수 없습니다.") Long themeId,
        @NotNull(message = "예약 타입은 비어있을 수 없습니다.") ReservationStatus status) {
}
