package roomescape.domain.reservation.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.reservation.application.dto.request.CreateReservationThemeServiceRequest;

public record CreateReservationThemeRequest(
        @NotBlank(message = "테마 이름을 빈 문자열이 아닌 값으로 입력해주세요.")
        String name,
        @NotBlank(message = "테마에 대한 설명을 빈 문자열이 아닌 값으로 입력해주세요.")
        String description,
        @NotBlank(message = "테마의 썸네일을 빈 문자열이 아닌 값으로 입력해주세요.")
        String thumbnail
) {

    public CreateReservationThemeServiceRequest toServiceRequest() {
        return new CreateReservationThemeServiceRequest(name, description, thumbnail);
    }
}
