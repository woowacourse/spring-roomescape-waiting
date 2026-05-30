package roomescape.theme.service.dto;

public record ThemeCommand(
        String name,
        String description,
        String thumbnailUrl
) {
}
