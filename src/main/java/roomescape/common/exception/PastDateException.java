package roomescape.common.exception;

import java.time.LocalDate;

public class PastDateException extends RuntimeException {
    public PastDateException(LocalDate date) {
        super("과거 시간은 예약 등록을 할 수 없습니다. date = " + date);
    }
}
