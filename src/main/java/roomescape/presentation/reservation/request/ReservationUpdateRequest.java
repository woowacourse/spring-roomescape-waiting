package roomescape.presentation.reservation.request;

import jakarta.validation.constraints.NotNull;

public record ReservationUpdateRequest(
        @NotNull(message = "슬롯은 필수 선택 사항 입니다. 슬롯을 선택해주세요.")
        Long slotId
) {
}
