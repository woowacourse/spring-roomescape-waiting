package roomescape.theme.controller.dto;

import roomescape.global.exception.InvalidRequestFormatException;


import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.global.exception.BadRequestException;

public record ThemeRequest(String name, String description, String thumbnailUrl) {

    public ThemeRequest {
        if (name == null || name.isBlank()
        || description == null || thumbnailUrl == null) {
            throw new InvalidRequestFormatException(ThemeErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ThemeCommand toCommand() {
        return new ThemeCommand(name, description, thumbnailUrl);
    }
}
