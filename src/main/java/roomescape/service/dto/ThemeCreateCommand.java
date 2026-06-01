package roomescape.service.dto;

public record ThemeCreateCommand(
        String name,
        String description,
        String thumbnailUrl
) {
}
