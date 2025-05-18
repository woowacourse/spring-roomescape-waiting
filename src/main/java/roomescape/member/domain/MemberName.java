package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.reservation.domain.util.ValidationUtils;

@Embeddable
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "name")
public class MemberName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 5;

    private String name;

    public MemberName(final String name) {
        validate(name);
        this.name = name;
    }

    private void validate(final String name) {
        ValidationUtils.validateNonNull(name, "멤버 이름은 필수입니다.");
        ValidationUtils.validateNonBlank(name, "멤버 이름은 공백이 아니어야 합니다.");
        validateLength(name);
    }

    private void validateLength(final String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("멤버 이름은 최소 %d글자, 최대 %d글자여야합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
