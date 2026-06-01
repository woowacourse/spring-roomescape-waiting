package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.reservation.domain.exception.NotFoundTimeSlotException;

public interface TimeSlotRepository {

    TimeSlot save(TimeSlot slot);
    Optional<TimeSlot> findByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId);

    default TimeSlot getByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return findByDateTimeAndTheme(date, timeId, themeId)
                .orElseThrow(() -> new NotFoundTimeSlotException("해당 날짜의 테마 시간이 없습니다."));
    }
}
