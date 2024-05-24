package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ThemeName {

    private static final int MAX_THEME_NAME_LENGTH = 30;
    private String value;

    public ThemeName(final String value) {
        validateName(value);
        this.value = value;
    }

    protected ThemeName() {

    }

    private void validateName(final String value) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 테마 이름을 입력해주세요.");
        }
        if (value.length() > MAX_THEME_NAME_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 테마 이름의 길이는 " + MAX_THEME_NAME_LENGTH + "자 이하입니다.");
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
        ThemeName themeName = (ThemeName) o;
        return Objects.equals(value, themeName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
