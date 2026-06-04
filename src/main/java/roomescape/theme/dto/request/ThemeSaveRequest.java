package roomescape.theme.dto.request;

public record ThemeSaveRequest(
        String name,
        String description,
        String thumbnailUrl
) {
}
