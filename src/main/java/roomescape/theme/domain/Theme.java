package roomescape.theme.domain;

public record Theme(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static Theme of(String name, String description, String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }
}
