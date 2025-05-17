package roomescape.common.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.validate.Validator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class Email {

    private String value;

    public static Email from(final String value) {
        validate(value);
        return new Email(value);
    }

    private static void validate(final String value) {
        Validator.of(Email.class)
                .validateEmailFormat(Fields.value, value, DomainTerm.EMAIL.label());
    }
}
