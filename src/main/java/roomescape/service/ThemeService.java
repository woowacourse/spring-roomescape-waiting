package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.AvailableTimes;
import roomescape.service.dto.PopularTheme;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationTimeRepository reservationTimeRepository) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<Theme> findThemes() {
        return themeRepository.findAll();
    }

    public AvailableTimes findAvailableTimes(long themeId, LocalDate date) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Long> reservedTimeIds = themeRepository.findReservedTimeIds(themeId, date);

        return AvailableTimes.from(reservationTimes, reservedTimeIds);
    }

    public List<PopularTheme> findPopularThemes(LocalDate endDate, int days, int limit) {
        LocalDate startDate = endDate.minusDays(days);
        List<Theme> themes = themeRepository.findPopularThemes(startDate, endDate, limit);

        return IntStream.range(0, themes.size())
                .mapToObj(idx -> PopularTheme.of(themes.get(idx), idx + 1))
                .toList();
    }

    @Transactional
    public Theme createTheme(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        return themeRepository.save(theme);
    }

    @Transactional
    public void deleteTheme(long id) {
        themeRepository.deleteById(id);
    }
}
