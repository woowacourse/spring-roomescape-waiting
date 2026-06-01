package roomescape.theme.dto;

public record PopularThemeResult(
        Long id,
        String name,
        String description,
        String imgUrl,
        Long rank,
        Long reservationCount
) {
    public static PopularThemeResult from(PopularThemeProjection projection) {
        return new PopularThemeResult(
                projection.id(),
                projection.name(),
                projection.description(),
                projection.imgUrl(),
                projection.rank(),
                projection.reservationCount()
        );
    }
}
