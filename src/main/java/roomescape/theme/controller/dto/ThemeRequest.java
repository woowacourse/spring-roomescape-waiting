package roomescape.theme.controller.dto;

import roomescape.theme.exception.InvalidThemeRequestFormatException;
import roomescape.theme.service.dto.ThemeCommand;

public record ThemeRequest(String name, String description, String thumbnailUrl) {

    public ThemeRequest {
        if (name == null || name.isBlank()
        || description == null || thumbnailUrl == null) {
            throw new InvalidThemeRequestFormatException();
        }
    }

    public ThemeCommand toCommand() {
        return new ThemeCommand(name, description, thumbnailUrl);
    }
}
