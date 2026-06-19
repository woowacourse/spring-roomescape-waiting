package roomescape.theme.application.dto.request;

public record ThemeSaveRequest(
        String name,
        String description,
        String thumbnailUrl,
        Integer price
) {
    public ThemeSaveRequest(String name, String description, String thumbnailUrl) {
        this(name, description, thumbnailUrl, null);
    }
}
