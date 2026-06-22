package roomescape.theme.repository.entity;

public record ThemeEntity(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        int price
) {
}
