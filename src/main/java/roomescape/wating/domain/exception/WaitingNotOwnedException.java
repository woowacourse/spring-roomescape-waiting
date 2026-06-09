package roomescape.wating.domain.exception;

import roomescape.common.exception.NotFoundException;

public class WaitingNotOwnedException extends NotFoundException {

    public WaitingNotOwnedException() {
        super("존재하지 않는 대기입니다.");
    }
}
