package roomescape.domain;

import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.ReservationException;

@Getter
public class PersonName {

    private final String name;

    public PersonName(final String value) {
        validate(value);
        this.name = value;
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new ReservationException(ErrorCode.PERSON_NAME_NULL_OR_BLANK);
        }
    }
}
