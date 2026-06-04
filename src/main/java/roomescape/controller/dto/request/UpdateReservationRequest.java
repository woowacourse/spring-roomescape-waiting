package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateReservationRequest(
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다") LocalDate date,
        @NotNull(message = "예약 시간은 비어있을 수 없습니다") Long timeId
) {
}
