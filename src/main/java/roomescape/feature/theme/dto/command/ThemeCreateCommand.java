package roomescape.feature.theme.dto.command;

public record ThemeCreateCommand(
    String name,
    String description,
    String imageUrl
) {
}
