package roomescape.domain.reservationtime.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeRequest(
    @NotNull(message = "시작 시간은 필수 입력 값입니다.")
    LocalTime startAt,

    @NotNull(message = "종료 시간은 필수 입력 값입니다.")
    LocalTime finishAt
) {

}
