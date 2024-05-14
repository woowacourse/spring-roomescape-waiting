package roomescape.member.domain;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

import java.util.Objects;

public class Password {
    private final String value;

    public Password(final String value) {
        this.value = value;

        validateBlank();
        validateLength();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(value)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("회원(Member) 비밀번호(Password) 유효하지 않은 값(null OR 공백)이 입력되었습니다. [values: %s]", this));
        }
    }

    private void validateLength() {
        if (value.length() < 8 || value.length() > 16) {
            throw new ValidateException(ErrorType.INVALID_MEMBER_PASSWORD,
                    String.format("회원(Member)의 비밀번호(PASSWORD)는 8자 이상 16자 이하여야 합니다. [Password: %s]", value));
        }
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Password{" +
                "value='" + value + '\'' +
                '}';
    }
}
