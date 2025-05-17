package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.CreateThemeParam;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.infrastructure.error.exception.ThemeException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public Long create(CreateThemeParam createThemeParam) {
        if (themeRepository.existsByName(createThemeParam.name())) {
            throw new ThemeException("이미 같은 이름의 테마가 존재합니다.");
        }
        Theme theme = themeRepository.save(new Theme(
                        createThemeParam.name(),
                        createThemeParam.description(),
                        createThemeParam.thumbnail()
                )
        );
        return theme.getId();
    }

    public ThemeResult findById(Long id) {
        Theme theme = getThemeById(id);
        return ThemeResult.from(theme);
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeException(themeId + "에 해당하는 theme 튜플이 없습니다."));
    }

    @Transactional
    public void deleteById(final Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new ThemeException("해당 테마에 예약이 존재합니다.");
        }
        themeRepository.deleteById(themeId);
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
