package roomescape.theme.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Embeddable;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

@Embeddable
public record Description(String description) {
    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 100;

    public Description(final String description) {
        this.description = description;

        validateBlank();
        validateLength();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(description)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    "테마(Theme) 설명(Description)에 유효하지 않은 값(null OR 공백)이 입력되었습니다.");
        }
    }

    private void validateLength() {
        if (description.length() < MIN_LENGTH || description.length() > MAX_LENGTH) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    String.format("테마 설명(Description)은 %d자 이상 %d자 이하여야 합니다.", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
