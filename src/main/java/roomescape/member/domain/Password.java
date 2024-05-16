package roomescape.member.domain;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

public record Password(String value) {

    public Password(final String value) {
        this.value = value;

        validateBlank();
        validateLength();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(value)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("회원(Member)의 비밀번호(Password)에 유효하지 않은 값(null OR 공백)이 입력되었습니다."));
        }
    }

    private void validateLength() {
        if (value.length() < 8 || value.length() > 16) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    String.format("회원(Member)의 비밀번호(Password)는 8자 이상 16자 이하여야 합니다."));
        }
    }
}

