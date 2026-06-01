package roomescape.theme.dto.request;

public record ThemeCreateRequest(
        String name,
        String description,
        String imageUrl
) {
}
