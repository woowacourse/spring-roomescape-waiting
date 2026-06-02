package roomescape.theme.domain.repository;

public record PopularTheme(Long id, String name, String description, String thumbnailImgUrl, int reservedCount) {
}
