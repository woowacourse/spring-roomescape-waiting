package roomescape.application.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeCreateRequest(
        @NotNull(message = "시간은 필수 사항 입니다. 시간을 선택해주세요.")
        LocalTime startAt
) {
}
