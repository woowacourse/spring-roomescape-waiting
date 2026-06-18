package roomescape.theme.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.holiday.repository.HolidayRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeSaveServiceRequest;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.TimeService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;
    private final TimeService timeService;
    private final HolidayRepository holidayRepository;
    private final ReservationRepository reservationRepository;
    private final int dayCount;
    private final int rankCount;

    public ThemeServiceImpl(
            ThemeRepository themeRepository,
            TimeService timeService,
            HolidayRepository holidayRepository,
            ReservationRepository reservationRepository,
            @Value("${theme.dayCount:7}") int dayCount,
            @Value("${theme.rankCount:10}") int rankCount
    ) {
        this.themeRepository = themeRepository;
        this.timeService = timeService;
        this.holidayRepository = holidayRepository;
        this.reservationRepository = reservationRepository;
        this.dayCount = dayCount;
        this.rankCount = rankCount;
    }

    @Override
    public List<Theme> getAll() {
        return themeRepository.findAll();
    }

    @Override
    @Transactional
    public Theme create(ThemeSaveServiceRequest theme) {
        Theme newTheme = new Theme(
                theme.name(),
                theme.description(),
                theme.imageUrl()
        );
        return themeRepository.save(newTheme);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if(!themeRepository.existsById(id)) {
            throw new ThemeNotFoundException(id);
        }
        themeRepository.deleteById(id);
    }

    @Override
    public List<ReservationTime> getAvailableTimes(Long themeId, LocalDate date) {
        if (!themeRepository.existsById(themeId)) {
            throw new ThemeNotFoundException(themeId);
        }

        if (holidayRepository.existsByDate(date)) {
            return List.of();
        }

        Set<Long> reservedTimeIds = new HashSet<>(reservationRepository.findAvailableTimeIds(themeId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
        return timeService.findByDate(date)
                .stream()
                .filter(time -> !reservedTimeIds.contains(time.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Theme> getBestThemes() {
        LocalDate today = LocalDate.now();
        return themeRepository.findBestThemesByDate(today.minusDays(dayCount), today.minusDays(1), rankCount);
    }
}
