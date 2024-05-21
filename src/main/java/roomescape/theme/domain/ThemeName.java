package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public record ThemeName(
        @Column(name = "name", length = 30, nullable = false, unique = true)
        String name) {
    private static final int MAX_LENGTH = 30;

    public ThemeName {
        Objects.requireNonNull(name);
        if (name.isEmpty() || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 이름은 1글자 이상 30글자 미만이어야 합니다.");
        }
    }
}
