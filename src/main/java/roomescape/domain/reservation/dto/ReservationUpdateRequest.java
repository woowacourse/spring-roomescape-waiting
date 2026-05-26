package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationUpdateRequest(
    @NotNull(message = "예약 날짜 선택은 필수입니다")
    Long dateId,

    @NotNull(message = "예약 시간 선택은 필수입니다")
    Long timeId
) {

}
