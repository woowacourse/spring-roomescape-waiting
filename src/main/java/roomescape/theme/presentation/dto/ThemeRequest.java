package roomescape.theme.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import roomescape.theme.application.dto.ThemeCommand;

@Builder
public record ThemeRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotBlank(message = "섬네일 이미지가 없습니다.")
        String thumbnailImageUrl,
        @NotBlank(message = "설명을 작성해주세요.")
        String description
) {
    public ThemeCommand toCommand() {
        return ThemeCommand.builder()
                .name(this.name)
                .description(this.description)
                .thumbnailImageUrl(this.thumbnailImageUrl)
                .build();
    }
}
