package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Getter
@Builder
public class TimeSlot {
    private Long id;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;

    public TimeSlot withId(long generatedId) {
        return TimeSlot.builder()
                .id(generatedId)
                .date(date)
                .time(time)
                .theme(theme)
                .build();
    }

    public boolean isPast(Clock clock) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now(clock));
    }

    public void checkChangeableTime(Clock clock) {
        time.validateDateTime(date, clock);
    }
}
