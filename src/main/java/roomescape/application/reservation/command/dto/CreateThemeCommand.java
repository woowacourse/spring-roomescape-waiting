package roomescape.application.reservation.command.dto;

public record CreateThemeCommand(
        String name,
        String description,
        String thumbnail
) {
}
