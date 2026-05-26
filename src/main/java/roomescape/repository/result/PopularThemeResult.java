package roomescape.repository.result;

public record PopularThemeResult(
        Long id,
        String name,
        String description,
        String thumbnail,
        Long reservationCount
) {
}
