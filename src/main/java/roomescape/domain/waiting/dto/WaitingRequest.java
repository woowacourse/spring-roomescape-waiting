package roomescape.domain.waiting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record WaitingRequest(
        @NotBlank(message = "예약할 사용자의 이름은 필수 입력 값입니다.")
        @Size(max = 255, message = "이름은 255자를 초과할 수 없습니다.")
        String name,

        @NotNull(message = "예약 날짜는 필수 입력 값입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간 id는 필수 입력 값입니다.")
        Long timeId,

        @NotNull(message = "예약 테마 id는 필수 입력 값입니다.")
        Long themeId
) {

}
