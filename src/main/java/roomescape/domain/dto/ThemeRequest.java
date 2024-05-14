package roomescape.domain.dto;

import roomescape.domain.Theme;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

public record ThemeRequest(String name, String description, String thumbnail) {
    public ThemeRequest {
        isValid(name, description, thumbnail);
    }

    private void isValid(final String name, final String description, final String thumbnail) {
        validEmpty("name", name);
        validEmpty("description", description);
        validEmpty("thumbnail", thumbnail);
    }

    private void validEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
        }
    }

    public Theme toEntity(final Long id) {
        return new Theme(id, name, description, thumbnail);
    }
}
