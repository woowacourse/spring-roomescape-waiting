package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingCreationRequest(
        @NotNull(message = "날짜는 빈 값을 허용하지 않습니다.")
        LocalDate date,

        @NotNull(message = "테마ID는 빈 값을 허용하지 않습니다.")
        Long themeId,

        @NotNull(message = "예약시간ID는 빈 값을 허용하지 않습니다.")
        Long timeId
) {

}
