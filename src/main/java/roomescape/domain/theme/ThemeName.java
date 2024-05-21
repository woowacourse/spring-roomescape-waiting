package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

@Embeddable
public class ThemeName {
    private static final int NAME_MAX_LENGTH = 20;

    @Column(nullable = false)
    private String name;

    public ThemeName(String name) {
        validateNonBlank(name);
        validateLength(name);
        this.name = name;
    }

    protected ThemeName() {
    }

    public String asText() {
        return name;
    }

    private void validateNonBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(HttpStatus.BAD_REQUEST, "테마명은 필수 입력값 입니다.");
        }
    }

    private void validateLength(String name) {
        if (name != null && name.length() > NAME_MAX_LENGTH) {
            throw new RoomescapeException(HttpStatus.BAD_REQUEST,
                    String.format("테마명은 %d자 이하여야 합니다.", NAME_MAX_LENGTH));
        }
    }
}
