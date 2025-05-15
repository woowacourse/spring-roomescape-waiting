package roomescape.theme.presentation.dto;

public record PopularThemeResponse(String name, String description, String thumbnail) {
    public PopularThemeResponse {
        if (name == null) {
            throw new IllegalArgumentException("name은 null 일 수 없습니다.");
        }

        if (thumbnail == null) {
            throw new IllegalArgumentException("thumbnail은 null 일 수 없습니다.");
        }

        if (description == null) {
            throw new IllegalArgumentException("description은 null 일 수 없습니다.");
        }
    }
}
