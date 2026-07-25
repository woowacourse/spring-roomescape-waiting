package roomescape.exception;

import roomescape.domain.Waiting;

public class DuplicateWaitingException extends RoomescapeException {

    public DuplicateWaitingException(Waiting waiting) {
        super("DUPLICATE_WAITING",
                String.format("해당 날짜(%s)의 시간(%d)과 테마(%d)는 이미 예약 대기되어 있습니다.",
                        waiting.getSession().getDate(),
                        waiting.getSession().getTimeSlot().getId(),
                        waiting.getSession().getTheme().getId()));
    }
}
