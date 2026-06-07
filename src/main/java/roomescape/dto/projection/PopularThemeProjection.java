package roomescape.dto.projection;

public record PopularThemeProjection(
        Long id,
        String name,
        String description,
        String imgUrl,
        Long rank,
        Long reservationCount
) {
}
