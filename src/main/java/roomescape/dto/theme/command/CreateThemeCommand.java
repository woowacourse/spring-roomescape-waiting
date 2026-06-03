package roomescape.dto.theme.command;

import roomescape.dto.theme.request.CreateThemeRequest;

public record CreateThemeCommand(
        String name,
        String description,
        String thumbnailImageUrl
) {
    public static CreateThemeCommand from(CreateThemeRequest request) {
        return new CreateThemeCommand(request.name(), request.description(), request.thumbnailImageUrl());
    }
}