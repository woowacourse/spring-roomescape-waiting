package roomescape.reservation.application;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.TimeSlotRepository;
import roomescape.reservation.domain.exception.NotFoundTimeSlotException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlot getTimeSlot(LocalDate date, ReservationTime time, Theme theme) {
        try {
            return timeSlotRepository.getByDateTimeAndTheme(date, time.getId(), theme.getId());
        } catch (NotFoundTimeSlotException e) {
            return timeSlotRepository.save(
                    TimeSlot.builder()
                            .date(date)
                            .time(time)
                            .theme(theme)
                            .build()
            );
        }
    }
}
