package roomescape.service.command;

public record ThemeRegisterCommand(
        String name,

        String description,

        String thumbnailImageUrl,

        int price
) {
    public ThemeRegisterCommand(String name, String description, String thumbnailImageUrl) {
        this(name, description, thumbnailImageUrl, 0);
    }
}
