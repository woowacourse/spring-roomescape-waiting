package roomescape.theme.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Embeddable;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

@Embeddable
public record ThemeName(String value) {

    public ThemeName(final String value) {
        this.value = value;
        validateBlank();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(value)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("테마 이름(ThemeName)에 유효하지 않은 값(null OR 공백)이 입력되었습니다."));
        }
    }
}

