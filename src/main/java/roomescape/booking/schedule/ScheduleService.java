package roomescape.booking.schedule;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.booking.schedule.dto.ScheduleRequest;
import roomescape.booking.schedule.dto.ScheduleResponse;
import roomescape.exception.custom.reason.schedule.ScheduleNotExistException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeService;
import roomescape.theme.Theme;
import roomescape.theme.ThemeService;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.reservationTimeId());
        Theme theme = themeService.findById(request.themeId());
        Schedule schedule = new Schedule(request.date(), reservationTime, theme);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleResponse.of(savedSchedule);
    }

    @Transactional(readOnly = true)
    public Schedule findByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(date, timeId, themeId)
                .orElseThrow(ScheduleNotExistException::new);
    }
}
