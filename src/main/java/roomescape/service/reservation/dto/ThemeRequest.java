package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.reservation.Theme;

public record ThemeRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        String name,
        @NotBlank(message = "설명을 입력해주세요.")
        String description,
        @NotBlank(message = "썸네일을 입력해주세요.")
        String thumbnail) {

    public Theme toEntity() {
        return new Theme(name, description, thumbnail);
    }
}
