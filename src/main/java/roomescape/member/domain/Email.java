package roomescape.member.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Embeddable;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

import java.util.regex.Pattern;

@Embeddable
public record Email(String email) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email(final String email) {
        this.email = email;

        validateBlank();
        validatePattern();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(email)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("회원(Member)의 이메일(Email)에 유효하지 않은 값(null OR 공백)이 입력되었습니다."));
        }
    }

    private void validatePattern() {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    String.format("입력하신 이메일(Email)의 형식이 잘못되었습니다."));
        }
    }
}
