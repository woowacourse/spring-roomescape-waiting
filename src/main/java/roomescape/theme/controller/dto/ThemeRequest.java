package roomescape.theme.controller.dto;

import roomescape.global.exception.InvalidRequestFormatException;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.service.dto.ThemeCommand;

public record ThemeRequest(
        String name,
        String description,
        String thumbnailUrl
) {

    public ThemeRequest {
        validateString(name);
        validateString(description);
        validateString(thumbnailUrl);
    }

    private void validateString(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestFormatException(ThemeErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ThemeCommand toCommand() {
        return new ThemeCommand(name, description, thumbnailUrl);
    }
}
