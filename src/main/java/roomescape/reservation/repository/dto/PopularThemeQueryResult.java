package roomescape.reservation.repository.dto;

public record PopularThemeQueryResult(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
}
