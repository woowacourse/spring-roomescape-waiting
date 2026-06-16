package roomescape.theme.repository.projection;

public record PopularThemeResult(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        boolean isActive,
        Long amount,
        long reservationCount
) {
}
