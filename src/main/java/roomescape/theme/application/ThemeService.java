package roomescape.theme.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ActiveReservationRepository;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.application.exception.DuplicateThemeException;
import roomescape.theme.application.exception.ThemeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ThemeService {

    private static final int WEEKS_BOUND = 1;
    private static final int DAYS_BOUND = 1;
    private static final int THEME_SIZE_LIMIT = 10;

    private final Clock clock;
    private final ThemeRepository themeRepository;
    private final ActiveReservationRepository reservationRepository;

    public ThemeInfo addTheme(final ThemeCommand theme) {
        if (themeRepository.existsByName(theme.name())) {
            throw new DuplicateThemeException("이미 존재하는 테마입니다.");
        }
        return ThemeInfo.from(themeRepository.save(theme.toEntity()));
    }

    public void deleteTheme(final Long id) {
        if (reservationRepository.existsByTheme(id)) {
            throw new ThemeInUseException("해당 테마의 예약이 존재합니다.");
        }
        Theme theme = themeRepository.getById(id)
                .delete(clock);
        themeRepository.delete(theme);
    }

    @Transactional(readOnly = true)
    public List<ThemeInfo> getThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeInfo> getWeeksTopThemes() {
        return themeRepository.findByReservationCountWithLimit(
                LocalDate.now(clock).minusWeeks(WEEKS_BOUND),
                LocalDate.now(clock).minusDays(DAYS_BOUND),
                THEME_SIZE_LIMIT
        ).stream()
        .map(ThemeInfo::from)
        .toList();
    }

    @Transactional(readOnly = true)
    public Theme getThemeById(Long id) {
        return themeRepository.getById(id);
    }
}
