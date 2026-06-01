package roomescape.domain.reservation.vo;

import org.springframework.util.StringUtils;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.type.GeneralErrorType;

public record ReserverName(String value) {

    private static final int MINIMUM_LENGTH = 1;
    private static final int MAXIMUM_LENGTH = 20;

    public ReserverName {
        if (!StringUtils.hasText(value) || isLengthOutOfRange(value)) {
            throw new GeneralException(GeneralErrorType.ILLEGAL_STATE);
        }
    }

    private boolean isLengthOutOfRange(String value) {
        return value.length() < MINIMUM_LENGTH || value.length() > MAXIMUM_LENGTH;
    }
}
