package roomescape.schedule;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.reason.schedule.ScheduleConflictException;
import roomescape.exception.custom.reason.schedule.ScheduleNotExistException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeService;
import roomescape.schedule.dto.ScheduleRequest;
import roomescape.schedule.dto.ScheduleResponse;
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
        ReservationTime reservationTime = reservationTimeService.getById(request.reservationTimeId());
        Theme theme = themeService.getById(request.themeId());
        validateDuplication(reservationTime, theme);

        Schedule schedule = new Schedule(request.date(), reservationTime, theme);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleResponse.of(savedSchedule);
    }

    @Transactional(readOnly = true)
    public Schedule getByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(date, timeId, themeId)
                .orElseThrow(ScheduleNotExistException::new);
    }

    private void validateDuplication(final ReservationTime reservationTime, final Theme theme) {
        if (scheduleRepository.existsByReservationTimeAndTheme(reservationTime, theme)) {
            throw new ScheduleConflictException();
        }
    }
}
