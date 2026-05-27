package roomescape.date.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReservationDateStatusUpdateDto(
    @NotNull(message = "isActive는 필수 입력입니다.")
    Boolean isActive
) {

}
