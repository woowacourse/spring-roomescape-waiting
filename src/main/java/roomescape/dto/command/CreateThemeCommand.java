package roomescape.dto.command;

public record CreateThemeCommand(
        String name,
        String description,
        String thumbnail
) {
}
