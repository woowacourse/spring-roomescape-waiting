package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class MemberEmail {

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    public static MemberEmail from(final String value) {
        validate(value);
        return new MemberEmail(value);
    }

    private static void validate(final String value) {
        Validator.of(MemberEmail.class)
                .emailField(MemberEmail.Fields.value, value);
    }
}
