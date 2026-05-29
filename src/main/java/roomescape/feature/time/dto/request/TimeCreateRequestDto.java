package roomescape.feature.time.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeCreateRequestDto(
    @NotNull(message = "예약 시간은 필수입니다.")
    LocalTime startAt
) {

}
