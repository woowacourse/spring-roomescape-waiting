package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.schdule.ReservationDate;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationScheduleRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationScheduleService {

    private final ReservationScheduleRepository reservationScheduleRepository;
    private final Clock clock;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationScheduleService(final ReservationScheduleRepository reservationScheduleRepository,
                                      final Clock clock, final ReservationTimeRepository reservationTimeRepository,
                                      final ThemeRepository themeRepository) {
        this.reservationScheduleRepository = reservationScheduleRepository;
        this.clock = clock;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        createSchedule();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void createSchedule() {
        Set<LocalDate> scheduledDates = existingScheduledDates();
        List<ReservationSchedule> newSchedules = generateNewSchedules(scheduledDates);
        reservationScheduleRepository.saveAll(newSchedules);
    }

    private Set<LocalDate> existingScheduledDates() {
        return reservationScheduleRepository.findAll().stream()
                .map(ReservationSchedule::getDate)
                .collect(Collectors.toSet());
    }

    private List<ReservationSchedule> generateNewSchedules(Set<LocalDate> scheduledDates) {
        List<ReservationTime> times = reservationTimeRepository.findAll();
        List<Theme> themes = themeRepository.findAll();
        LocalDate now = LocalDate.now(clock);
        LocalDate end = now.plusMonths(2);

        return now.datesUntil(end)
                .filter(date -> !scheduledDates.contains(date))
                .flatMap(date -> themeTimeCombinations(date, times, themes).stream())
                .toList();
    }

    private List<ReservationSchedule> themeTimeCombinations(LocalDate date, List<ReservationTime> times,
                                                            List<Theme> themes) {
        return themes.stream()
                .flatMap(theme -> times.stream()
                        .map(time -> new ReservationSchedule(null, new ReservationDate(date), time, theme)))
                .toList();
    }
}
