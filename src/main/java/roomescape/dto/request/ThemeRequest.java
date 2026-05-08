package roomescape.dto.request;

public record ThemeRequest(
        String name,
        String thumbnailUrl,
        String description
) {
}
