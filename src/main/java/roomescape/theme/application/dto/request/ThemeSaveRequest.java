package roomescape.theme.application.dto.request;

public record ThemeSaveRequest(
        String name,
        String description,
        String thumbnailUrl
) {
}
