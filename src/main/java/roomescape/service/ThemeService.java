package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private static final int RANKING_LIMIT = 10;
    public static final int MAX_RANKING_PERIOD = 366;

    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public Theme save(Theme themeWithoutId) {
        return themeRepository.save(themeWithoutId);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        themeRepository.delete(id);
    }

    public List<Theme> findRanking(LocalDate startDate, LocalDate endDate) {
        validateRankingPeriod(startDate, endDate);

        return themeRepository.findRanking(startDate, endDate, RANKING_LIMIT);
    }

    public Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_THEME));
    }

    public void validateExistTheme(Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_FOUND_THEME);
        }
    }

    private void validateRankingPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDate localDate = LocalDate.now(clock);

        if (startDate.isAfter(localDate) || endDate.isAfter(localDate)) {
            throw new CustomInvalidRequestException(ErrorCode.FUTURE_RANKING_PERIOD);
        }
        if (startDate.isAfter(endDate)) {
            throw new CustomInvalidRequestException(ErrorCode.INVALID_RANKING_PERIOD);
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_RANKING_PERIOD) {
            throw new CustomInvalidRequestException(ErrorCode.LONG_RANKING_PERIOD);
        }
    }
}
