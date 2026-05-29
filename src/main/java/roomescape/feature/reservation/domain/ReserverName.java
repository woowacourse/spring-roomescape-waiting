package roomescape.feature.reservation.domain;

import org.springframework.util.StringUtils;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.type.GeneralErrorType;

public record ReserverName(String value) {

    private static final int MAXIMUM_LENGTH = 20;

    public ReserverName {
        if (!StringUtils.hasText(value) || value.length() > MAXIMUM_LENGTH) {
            throw new GeneralException(GeneralErrorType.ILLEGAL_STATE);
        }
    }
}
