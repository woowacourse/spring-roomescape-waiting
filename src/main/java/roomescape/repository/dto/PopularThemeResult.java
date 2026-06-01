package roomescape.repository.dto;

public record PopularThemeResult(
        Long id,
        String name,
        String description,
        String thumbnail,
        Long reservationCount
) {
}
