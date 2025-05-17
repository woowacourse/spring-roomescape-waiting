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
public class MemberName {

    @Column(name = "name", nullable = false)
    private String value;

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
