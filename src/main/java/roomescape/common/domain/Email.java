package roomescape.common.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.validate.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class Email {

    private final String value;

    public static Email from(final String value) {
        validate(value);
        return new Email(value);
    }

    private static void validate(final String value) {
        Validator.of(Email.class)
                .validateEmailFormat(Fields.value, value, DomainTerm.EMAIL.label());
    }
}
