package roomescape.domain.theme;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ThemeName {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 30;

    @Column(name = "name", nullable = false, length = 20)
    private String value;

    protected ThemeName() {
    }

    public ThemeName(String value) {
        Objects.requireNonNull(value);
        String striped = value.strip();
        validate(striped);
        this.value = striped;
    }

    private void validate(String value) {
        if (MIN_NAME_LENGTH > value.length() || MAX_NAME_LENGTH < value.length()) {
            throw new RoomEscapeException(ErrorCode.INVALID_THEME_NAME_LENGTH);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThemeName themeName = (ThemeName) o;
        return Objects.equals(value, themeName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
