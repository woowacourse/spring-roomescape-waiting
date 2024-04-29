package roomescape.domain.theme;

import java.util.Objects;
import roomescape.global.exception.RoomescapeException;

public class Description {

    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final String value;

    public Description(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new RoomescapeException("테마 설명은 null이거나 비어 있을 수 없습니다.");
        }
        if (value.length() > MAX_DESCRIPTION_LENGTH) {
            throw new RoomescapeException(String.format("테마 길이는 최대 %d글자입니다.", MAX_DESCRIPTION_LENGTH));
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Description that = (Description) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
