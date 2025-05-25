package roomescape.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.repository.GameScheduleRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class GameScheduleService {

    private final GameScheduleRepository gameScheduleRepository;
    private final ThemeService themeService;
    private final TimeService timeService;

    public GameScheduleService(
            GameScheduleRepository gameScheduleRepository,
            ThemeService themeService,
            TimeService timeService
    ) {
        this.gameScheduleRepository = gameScheduleRepository;
        this.themeService = themeService;
        this.timeService = timeService;
    }

    @Transactional
    public GameSchedule createGameSchedule(LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        ReservationTime time = timeService.getTimeEntityById(timeId);
        validateNotPast(date, time.getStartAt(), now);
        Theme theme = themeService.getThemeEntityById(themeId);

        GameSchedule gameSchedule = GameSchedule.withoutId(date, time, theme);
        return gameScheduleRepository.save(gameSchedule);
    }

    private void validateNotPast(LocalDate date, LocalTime time, LocalDateTime now) {
        LocalDateTime schedule = LocalDateTime.of(date, time);
        if (schedule.isBefore(now)) {
            throw new IllegalArgumentException("과거 일시로 예약할 수 없습니다.");
        }
    }

    public GameSchedule getGameScheduleEntityById(Long id) {
        return gameScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("삭제하려는 게임 일정 id가 존재하지 않습니다. id: " + id));
    }

    public GameSchedule getGameScheduleEntityBy(LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        return gameScheduleRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseGet(() -> createGameSchedule(date, timeId, themeId, now));
    }
}
