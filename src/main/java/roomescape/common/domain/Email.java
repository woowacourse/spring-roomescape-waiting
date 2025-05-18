package roomescape.common.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import roomescape.common.validate.InvalidArgumentException;
import roomescape.common.validate.ValidationType;
import roomescape.common.validate.Validator;

import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private String value;

    public static Email from(final String value) {
        validate(value);
        return new Email(value);
    }

    private static void validate(final String value) {
        Validator.of(Email.class)
                .validateNotBlank(Fields.value, value, DomainTerm.EMAIL.label());

        if (EMAIL_PATTERN.matcher(value).matches()) {
            return;
        }

        throw new InvalidArgumentException(
                ValidationType.EMAIL_CHECK,
                Email.class.getSimpleName(),
                Fields.value,
                DomainTerm.EMAIL.label());
    }
}
