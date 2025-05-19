package roomescape.application.reservation.query;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.query.dto.ThemeResult;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeQueryService {

    private static final int RANK_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ThemeQueryService(ThemeRepository themeRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public List<ThemeResult> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public List<ThemeResult> findRankBetweenDate() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today.minusDays(1);
        List<Theme> rankForWeek = themeRepository.findRankBetweenDate(startDate, endDate, RANK_LIMIT);
        return rankForWeek.stream()
                .map(ThemeResult::from)
                .toList();
    }
}
