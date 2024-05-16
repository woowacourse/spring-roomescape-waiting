package roomescape.theme.domain;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

public record Thumbnail(String value) {

    public Thumbnail(final String value) {
        this.value = value;
        validateBlank();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(value)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("테마 썸네일(Thumbnail)에 유효하지 않은 값(null OR 공백)이 입력되었습니다."));
        }
    }
}

