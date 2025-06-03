package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public record ThemeName(
        @Column(nullable = false)
        @Size(max = ThemeName.MAX_NAME_LENGTH)
        String name
) {

    private static final int MAX_NAME_LENGTH = 5;

    public ThemeName(final String name) {
        this.name = Objects.requireNonNull(name, "name은 null일 수 없습니다.");
        if (name.isBlank()) {
            throw new IllegalStateException("name은 공백일 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalStateException("name은 " + MAX_NAME_LENGTH + "자 이내여야 합니다.");
        }
    }
}
