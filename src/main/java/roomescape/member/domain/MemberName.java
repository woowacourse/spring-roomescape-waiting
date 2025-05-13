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
public class MemberName {

    private final String value;

    public static MemberName from(final String name) {
        validate(name);
        return new MemberName(name);
    }

    private static void validate(final String value) {
        Validator.of(MemberName.class)
                .notNullField(MemberName.Fields.value, value)
                .notBlankField(MemberName.Fields.value, value.strip());
    }
}
