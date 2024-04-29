package roomescape.domain.member;

import java.util.regex.Pattern;
import roomescape.global.exception.RoomescapeException;

public class Password {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        String.format("^(?=.*[a-zA-Z])(?=.*[0-9]).{%d,%d}$", MIN_LENGTH, MAX_LENGTH));

    private final String value;

    public Password(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null) {
            throw new RoomescapeException("패스워드는 null일 수 없습니다.");
        }
        if (!PASSWORD_PATTERN.matcher(value).matches()) {
            throw new RoomescapeException(
                String.format("패스워드는 %d자 이상 %d자 이하의 영문, 숫자 조합이어야 합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }

    public String getValue() {
        return value;
    }
}
