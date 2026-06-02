package roomescape.domain;

import lombok.Getter;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Getter
public class PersonName {

    private final String name;

    public PersonName(final String value) {
        validate(value);
        this.name = value;
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PERSON_NAME_NULL_OR_BLANK);
        }
    }
}
