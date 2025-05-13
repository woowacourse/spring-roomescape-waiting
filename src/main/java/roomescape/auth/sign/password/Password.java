package roomescape.auth.sign.password;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class Password {

    private final String encodedValue;

    public static Password fromRaw(final String rawPassword, final PasswordEncoder encoder) {
        validate(rawPassword);
        return encoder.execute(rawPassword);
    }

    public static Password fromEncoded(final String encodedPassword) {
        validate(encodedPassword);
        return new Password(encodedPassword);
    }

    private static void validate(final String value) {
        Validator.of(Password.class)
                .validateNotBlank(Fields.encodedValue, value, DomainTerm.USER_PASSWORD.label());
    }

    public boolean matches(final String rawPassword, final PasswordEncoder encoder) {
        return encoder.execute(rawPassword).equals(this);
    }
}
