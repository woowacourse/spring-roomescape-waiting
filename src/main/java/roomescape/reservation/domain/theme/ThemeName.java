package roomescape.reservation.domain.theme;

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
public class ThemeName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;

    private String name;

    public ThemeName(final String name) {
        validate(name);
        this.name = name;
    }

    private void validate(final String name) {
        ValidationUtils.validateNonNull(name, "테마 이름은 필수입니다.");
        ValidationUtils.validateNonBlank(name, "테마 이름은 공백이 아니어야 합니다.");
        validateLength(name);
    }

    private void validateLength(final String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 이름은 최소 %d글자, 최대 %d글자여야합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
