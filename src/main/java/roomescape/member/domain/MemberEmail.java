package roomescape.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class MemberEmail {

    private final String value;

    public static MemberEmail from(final String value) {
        validate(value);
        return new MemberEmail(value);
    }

    private static void validate(final String value) {
        Validator.of(MemberEmail.class)
                .emailField(MemberEmail.Fields.value, value);
    }
}
