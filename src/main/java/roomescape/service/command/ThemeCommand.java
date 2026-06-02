package roomescape.service.command;

public record ThemeCommand(
        String name,
        String thumbnailUrl,
        String description
) {
}
