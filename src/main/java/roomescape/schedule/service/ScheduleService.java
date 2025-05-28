package roomescape.schedule.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.ScheduleRepository;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public Schedule createAndSaveSchedule(LocalDate date, ReservationTime reservationTime, Theme theme) {
        Schedule schedule = new Schedule(null, date, reservationTime, theme);
        return save(schedule);
    }

    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Schedule getScheduleByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return scheduleRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).orElseThrow(() -> new BadRequestException("예약 가능한 일정이 존재하지 않습니다."));
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return scheduleRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
