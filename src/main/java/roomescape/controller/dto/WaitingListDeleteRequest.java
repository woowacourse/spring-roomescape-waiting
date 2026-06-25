package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WaitingListDeleteRequest(
        @NotNull(message = "예약 대기자 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "예약 대기자 이름은 비워둘 수 없습니다.")
        String name
) {
}
