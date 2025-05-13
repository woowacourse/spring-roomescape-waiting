package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.reservation.dto.CreateThemeParam;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@Service
public class ThemeService {

    private static final int RANK_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public List<ThemeResult> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public Long create(CreateThemeParam createThemeParam) {
        Theme theme = new Theme(createThemeParam.name(), createThemeParam.description(), createThemeParam.thumbnail());
        return themeRepository.create(theme);
    }

    public ThemeResult findById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("id에 해당하는 Theme이 없습니다."));
        return ThemeResult.from(theme);
    }

    public void deleteById(final Long themeId) {
        if (reservationRepository.existByThemeId(themeId)) {
            throw new BusinessRuleViolationException("해당 테마에 예약이 존재합니다.");
        }
        themeRepository.deleteById(themeId);
    }

    public List<ThemeResult> findRankBetweenDate() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);
        List<Theme> rankForWeek = themeRepository.findRankBetweenDate(startDate, endDate, RANK_LIMIT);
        return rankForWeek.stream()
                .map(ThemeResult::from)
                .toList();
    }
}
