package roomescape.user.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class UserName {

    private String value;

    public static UserName from(final String value) {
        validate(value);
        return new UserName(value);
    }

    private static void validate(final String value) {
        Validator.of(UserName.class)
                .validateNotBlank(Fields.value, value, DomainTerm.USER_NAME.label());
    }
}
