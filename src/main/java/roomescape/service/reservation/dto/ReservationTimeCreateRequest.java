package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record ReservationTimeCreateRequest(
        @NotBlank(message = "시간을 입력해주세요.") String startAt) {
}
