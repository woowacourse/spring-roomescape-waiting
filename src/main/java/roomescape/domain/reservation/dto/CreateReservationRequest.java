package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
    @NotBlank(message = "이름은 비어있을 수 없습니다.")
    String name,

    @NotNull(message = "날짜는 필수 선택 사항 입니다. 날짜를 선택해주세요.")
    Long dateId,

    @NotNull(message = "시간은 필수 선택 사항 입니다. 시간을 선택해주세요.")
    Long timeId,

    @NotNull(message = "테마는 필수 선택 사항 입니다. 테마를 선택해주세요.")
    Long themeId
) {

}
