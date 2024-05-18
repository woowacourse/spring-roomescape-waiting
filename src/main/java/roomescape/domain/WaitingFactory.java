package roomescape.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class WaitingFactory {

    public Waiting create(Member member, LocalDate date, Time time, Theme theme, Clock clock) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        validateRequestDateAfterCurrentTime(dateTime, clock);
        return new Waiting(member, date, time, theme);
    }

    private void validateRequestDateAfterCurrentTime(LocalDateTime dateTime, Clock clock) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        if (dateTime.isBefore(currentTime)) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }
}
