package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@Component
@RequiredArgsConstructor
public class ReservationSlotBatchGenerator {

    private static final int DAYS_IN_ADVANCE = 14;

    private final ReservationSlotRepository slotRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateReservationSlotsScheduled() {
        LocalDate targetDate = LocalDate.now(clock).plusDays(DAYS_IN_ADVANCE);
        generateSlotsForDate(targetDate);
    }

    @Transactional
    public void generateSlotsForDate(LocalDate date) {
        List<Theme> themes = themeRepository.findAll();
        List<ReservationTime> times = timeRepository.findAll();

        themes.forEach(theme -> times.stream()
                .filter(time -> !slotRepository.existsByDateAndThemeIdAndTimeId(date, theme.getId(), time.getId()))
                .map(time -> ReservationSlot.create(date, time, theme))
                .forEach(slotRepository::save)
        );
    }
}
