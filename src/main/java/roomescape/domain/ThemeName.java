package roomescape.domain;

import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public class ThemeName {
    private static final int NAME_MAX_LENGTH = 20;

    private final String name;

    public ThemeName(String name) {
        validateNonBlank(name);
        validateLength(name);
        this.name = name;
    }

    public String asText() {
        return name;
    }

    private void validateNonBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "테마명은 필수 입력값 입니다.");
        }
    }

    private void validateLength(String name) {
        if (name != null && name.length() > NAME_MAX_LENGTH) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, String.format("테마명은 %d자 이하여야 합니다.", NAME_MAX_LENGTH));
        }
    }
}
