package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTimeRequest(
    @NotBlank(message = "예약 날짜는 비어있을 수 없습니다.")
    String startAt
) {

}
