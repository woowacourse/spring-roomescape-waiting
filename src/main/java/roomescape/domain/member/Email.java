package roomescape.domain.member;

import java.util.regex.Pattern;
import roomescape.global.exception.RoomescapeException;

public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private final String value;

    public Email(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null) {
            throw new RoomescapeException("이메일은 null일 수 없습니다.");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new RoomescapeException("올바르지 않은 이메일 형식입니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
