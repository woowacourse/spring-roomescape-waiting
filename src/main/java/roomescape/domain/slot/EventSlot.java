package roomescape.domain.slot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;

public record EventSlot(
        LocalDate date,
        ReservationTime time,
        Theme theme
) {

    public static EventSlot from(LocalDate date, ReservationTime time, Theme theme) {
        return new EventSlot(date, time, theme);
    }

    public void verifyBookable(LocalDateTime now) {
        LocalDate today = now.toLocalDate();

        if (date.isBefore(today)) {
            throw new UnprocessableEntityException("과거 날짜로는 예약 대기를 할 수 없습니다.");
        }

        if (date.isEqual(today) && time.isBefore(now.toLocalTime())) {
            throw new UnprocessableEntityException("이미 지난 시간으로 예약 대기를 할 수 없습니다.");
        }
    }
}
