package roomescape.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Password {

    private final String value;

    public static Password from(final String password) {
        validate(password);
        return new Password(password);
    }

    private static void validate(final String value) {
        Validator.of(Password.class)
                .notNullField(Password.Fields.value, value)
                .notBlankField(Password.Fields.value, value.strip());
    }

    public boolean matches(PasswordEncoder passwordEncoder, String password) {
        return passwordEncoder.matches(password, value);
    }
}
