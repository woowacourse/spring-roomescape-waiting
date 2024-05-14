package roomescape.domain;

import java.util.regex.Pattern;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public class Email {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^(.+)@(\\S+)$");
    private static final int MIN_LENGTH = 11;
    private static final int MAX_LENGTH = 40;

    private final String email;

    public Email(String email) {
        if (email == null || email.isBlank()) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "이메일은 필수 입력값 입니다.");
        }
        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "이메일 형식이 올바르지 않습니다.");
        }
        int emailLength = email.length();
        if (emailLength < MIN_LENGTH || MAX_LENGTH < emailLength) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST,
                    String.format("이메일의 길이는 최소 %d자 이상 %d자 이하만 가능합니다. 현재 이메일 길이:%d", MIN_LENGTH, MAX_LENGTH, emailLength));
        }
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
