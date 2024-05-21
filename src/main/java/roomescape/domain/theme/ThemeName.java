package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ThemeName {

    private static final int MAX_LENGTH = 30;

    @Column(nullable = false, unique = true, length = MAX_LENGTH)
    private String name;

    protected ThemeName() {
    }

    protected ThemeName(String name) {
        validateBlank(name);
        validateLength(name);
        this.name = name;
    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마명은 필수 값입니다.");
        }
    }

    private void validateLength(String name) {
        if (MAX_LENGTH < name.length()) {
            throw new IllegalArgumentException(String.format("테마명은 %d자를 넘을 수 없습니다.", MAX_LENGTH));
        }
    }

    protected String getValue() {
        return name;
    }
}
