package roomescape.dto.command;

public record ThemeCommand(
        String name,
        String description,
        String thumbnail
) {
}
