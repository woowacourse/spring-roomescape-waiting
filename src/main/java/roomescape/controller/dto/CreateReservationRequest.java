package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
    @NotNull(message = "멤버 id는 null을 허용하지 않습니다.")
    Long memberId,
    @NotBlank(message = "날짜 정보는 비어있을 수 없습니다.")
    String date,
    @NotNull(message = "예약 시간 id는 null을 허용하지 않습니다.")
    Long timeId,
    @NotNull(message = "테마 id는 null을 허용하지 않습니다.")
    Long themeId) {

}
