package roomescape.reservation.application;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.TimeSlotRepository;
import roomescape.reservation.domain.exception.NotFoundTimeSlotException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TimeSlot getTimeSlot(LocalDate date, ReservationTime time, Theme theme) {
        try {
            return timeSlotRepository.getByDateTimeAndTheme(date, time.getId(), theme.getId());
        } catch (NotFoundTimeSlotException e) {
            return getOrCreateTimeSlot(date, time, theme);
        }
    }

    private TimeSlot getOrCreateTimeSlot(LocalDate date, ReservationTime time, Theme theme) {
        try {
            return timeSlotRepository.save(
                    TimeSlot.builder()
                            .date(date)
                            .time(time)
                            .theme(theme)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            return timeSlotRepository.getByDateTimeAndTheme(date, time.getId(), theme.getId());
        }
    }
}
