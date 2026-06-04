package roomescape.domain;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

public record PersonName(
        String name
) {

    public PersonName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PERSON_NAME_NULL_OR_BLANK);
        }
    }

    public boolean isSameName(final String name) {
        return this.name.equals(name);
    }
}
