package roomescape.theme.dto;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

public record ThemeRequest(
        String name,

        String description,

        String thumbnail
) {
    public ThemeRequest {
        if (StringUtils.isBlank(thumbnail) || StringUtils.isBlank(name) || StringUtils.isBlank(description)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("공백 또는 null이 포함된 테마(Theme) 등록 요청입니다. [values: %s]", this));
        }
    }
}
