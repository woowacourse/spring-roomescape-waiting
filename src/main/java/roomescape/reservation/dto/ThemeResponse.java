package roomescape.reservation.dto;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeResponse from(final ThemeDto theme) {
        return new ThemeResponse(
                theme.id(),
                theme.name().getValue(),
                theme.description().getValue(),
                theme.thumbnail().getValue()
        );
    }
}
