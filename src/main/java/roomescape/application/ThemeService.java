package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byThemeId;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InUseException;

@Service
@AllArgsConstructor
public class ThemeService {

    private static final int MAX_POPULAR_THEME_COUNT = 10;

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public Theme register(final String name, final String description, final String thumbnail) {
        var theme = new Theme(name, description, thumbnail);
        return themeRepository.save(theme);
    }

    @Transactional(readOnly = true)
    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    @Transactional
    public void removeById(final long id) {
        if (reservationRepository.exists(byThemeId(id))) {
            throw new InUseException("삭제하려는 테마를 사용하는 예약이 있습니다.");
        }

        var theme = themeRepository.getById(id);
        themeRepository.delete(theme);
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes(final LocalDate startDate, final LocalDate endDate, final int count) {
        var finalCount = Math.min(count, MAX_POPULAR_THEME_COUNT);
        return themeRepository.findRankingByPeriod(startDate, endDate, finalCount);
    }
}
