package roomescape.exception;

import java.time.LocalDate;

public class DuplicateSessionException extends RoomescapeException {

    public DuplicateSessionException(LocalDate date, Long timeId, Long themeId) {
        super("DUPLICATE_SESSION",
                String.format("해당 날짜(%s)의 시간(%d)과 테마(%d) 조합의 세션이 이미 존재합니다.", date, timeId, themeId));
    }
}
