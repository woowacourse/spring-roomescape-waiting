package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.require;
import static roomescape.domain.DomainPreconditions.requireNonBlank;

@Embeddable
public class ThemeName {
    private static final int MAX_NAME_LENGTH = 30;

    @Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
    private String value;

    protected ThemeName() {
    }

    public ThemeName(String value) {
        requireNonBlank(value, INVALID_INPUT, "테마 이름은 비어있을 수 없습니다.");
        String striped = value.strip();
        require(isValidLength(striped), INVALID_INPUT, "테마 이름은 1 ~ 30자여야 합니다.");
        this.value = striped;
    }

    private boolean isValidLength(String value) {
        return value.length() <= MAX_NAME_LENGTH;
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
