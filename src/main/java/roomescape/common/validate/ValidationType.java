package roomescape.common.validate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ValidationType {
    NULL_CHECK("while checking null"),
    BLANK_CHECK("while checking blank"),
    URI_CHECK("while checking URI"),
    EMAIL_CHECK("while checking email"),
    ;

    private final String description;
}
