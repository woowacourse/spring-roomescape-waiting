package roomescape.service.command;

public record ThemeRegisterCommand(
        String name,

        String description,

        String thumbnailImageUrl,

        Long price
) {
}
