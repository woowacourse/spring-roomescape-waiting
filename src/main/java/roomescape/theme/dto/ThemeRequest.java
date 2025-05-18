package roomescape.theme.dto;

import roomescape.common.exception.InvalidNameException;
import roomescape.common.exception.InvalidStringException;
import roomescape.common.exception.message.RequestExceptionMessage;

public record ThemeRequest(String name, String description, String thumbnail) {

    public ThemeRequest {
        if (name == null || name.isBlank()) {
            throw new InvalidNameException(RequestExceptionMessage.INVALID_NAME.getMessage());
        }
        if (description == null || description.isBlank()) {
            throw new InvalidStringException(RequestExceptionMessage.INVALID_STRING.getMessage());
        }
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new InvalidStringException(RequestExceptionMessage.INVALID_STRING.getMessage());
        }
    }
}
