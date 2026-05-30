package roomescape.feature.theme.dto.request;

public record ThemeCreateRequestDto(
    String name,
    String description,
    String imageUrl
) {

}
