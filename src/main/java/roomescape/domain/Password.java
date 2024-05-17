package roomescape.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Embeddable
public class Password {
    private static final Pattern PASSWORD_REGEX = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$", Pattern.CASE_INSENSITIVE);

    private String password;

    protected Password() {
    }

    public Password(String password) {
        if (password == null || password.isBlank()) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "비밀번호는 필수 입력값 입니다.");
        }
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST,
                    "비밀번호는 8~20자 범위의 최소 하나의 대소문자, 숫자, 특수문자가 포함되어야 합니다.");
        }
        this.password = password;
    }

    public boolean matches(String password) {
        return this.password.matches(password);
    }

    public String getPassword() {
        return password;
    }
}
