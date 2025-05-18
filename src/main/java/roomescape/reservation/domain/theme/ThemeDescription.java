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
@EqualsAndHashCode(of = "description")
public class ThemeDescription {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 225;

    private String description;

    public ThemeDescription(final String description) {
        validate(description);
        this.description = description;
    }

    private void validate(final String description) {
        ValidationUtils.validateNonNull(description, "테마 설명은 필수입니다.");
        ValidationUtils.validateNonBlank(description, "테마 설명은 공백이 아니어야 합니다.");
        validateLength(description);
    }

    private void validateLength(final String description) {
        if (description.length() < MIN_LENGTH || description.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 설명은 최소 %d글자, 최대 %d글자여야합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
