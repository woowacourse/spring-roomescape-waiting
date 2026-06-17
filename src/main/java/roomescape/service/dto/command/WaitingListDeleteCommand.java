package roomescape.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WaitingListDeleteCommand(
        @NotNull(message = "예약 대기 ID는 비워둘 수 없습니다.")
        Long waitingListId,

        @NotNull(message = "예약 대기자 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "예약 대기자 이름은 비워둘 수 없습니다.")
        String name
) {
}
