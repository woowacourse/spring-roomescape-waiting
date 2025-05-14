package roomescape.dto.request;

import roomescape.model.Theme;

public record ThemeRegisterDto(
        String name,
        String description,
        String thumbnail
) {
    public ThemeRegisterDto {
        validateRequiredFields(name, description, thumbnail);
    }

    private void validateRequiredFields(String name, String description, String thumbnail) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마명은 null이거나 공백일 수 없습니다");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 null이거나 공백일 수 없습니다");
        }

        if (thumbnail == null || thumbnail.isBlank()) {
            throw new IllegalArgumentException("썸내일 이미지는 null이거나 공백일 수 없습니다");
        }
    }

    public Theme convertToTheme() {
        return new Theme(name, description, thumbnail);
    }
}
