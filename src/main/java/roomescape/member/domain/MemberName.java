package roomescape.member.domain;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

public record MemberName(String value) {

    public MemberName(final String value) {
        this.value = value;

        validateBlank();
        validateName();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(value)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("회원 이름(MemberName)에 유효하지 않은 값(null OR 공백)이 입력되었습니다."));
        }
    }

    private void validateName() {
        if (value.length() < 1 || value.length() > 10) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    String.format("회원 이름(MemberName)은 1자 이상 10자 이하여야 합니다."));
        }
    }
}
