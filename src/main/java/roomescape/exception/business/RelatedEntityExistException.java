package roomescape.exception.business;

import roomescape.exception.ErrorCode;
import roomescape.exception.RootBusinessException;

public class RelatedEntityExistException extends RootBusinessException {

    public RelatedEntityExistException(final ErrorCode code) {
        super(code);
    }
}
