package roomescape.user.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class UserName {

    private final String value;

    public static UserName from(final String value) {
        validate(value);
        return new UserName(value);
    }

    private static void validate(final String value) {
        Validator.of(UserName.class)
                .validateNotBlank(Fields.value, value, DomainTerm.USER_NAME.label());
    }
}
