package roomescape.theme.domain.exception;

import roomescape.common.exception.ConflictException;

public class ThemeInUseException extends ConflictException {

    public ThemeInUseException(final Throwable cause) {
        super("해당 테마에 예약이 존재하여 삭제할 수 없습니다.", cause);
    }
}
