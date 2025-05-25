package roomescape.reservation.domain.theme;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.common.exception.RoomescapeException;

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
        validateMissing(description);
        validateLength(description);
    }

    private void validateMissing(final String description) {
        if (description == null || description.isBlank()) {
            throw new RoomescapeException("테마 설명은 null 또는 공백이 아니어야 합니다.");
        }
    }

    private void validateLength(final String description) {
        if (description.length() < MIN_LENGTH || description.length() > MAX_LENGTH) {
            throw new RoomescapeException(String.format("테마 설명은 최소 %d글자, 최대 %d글자여야합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
