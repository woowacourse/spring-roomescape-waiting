package roomescape.global.validation;

import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.customException.BadRequestException;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void requireNotBlank(String value, ErrorCode errorCode) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(errorCode);
        }
    }

    public static void requireNotNull(Object value, ErrorCode errorCode) {
        if (value == null) {
            throw new BadRequestException(errorCode);
        }
    }
}
