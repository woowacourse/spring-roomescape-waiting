package roomescape.domain.theme.dto.command;

public record ThemeCreateCommand(
    String name,
    String description,
    String imageUrl
) {
}
