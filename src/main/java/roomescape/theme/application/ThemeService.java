package roomescape.theme.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.application.exception.DuplicateThemeException;
import roomescape.theme.application.exception.ThemeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ThemeService {

    private static final int WEEKS_BOUND = 1;
    private static final int DAYS_BOUND = 1;
    private static final int THEME_SIZE_LIMIT = 10;

    private final Clock clock;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ThemeInfo create(ThemeCommand command) {
        if (themeRepository.existsByName(command.name())) {
            throw new DuplicateThemeException("이미 존재하는 테마입니다.");
        }

        Theme theme = themeRepository.save(command.toEntity());
        return ThemeInfo.from(theme);
    }

    @Transactional
    public void deactivate(Long id) {
        Theme theme = themeRepository.getById(id);

        if (reservationRepository.existsByTheme(id)) {
            throw new ThemeInUseException("예약이 존재하는 테마는 비활성화할 수 없습니다.");
        }

        themeRepository.update(theme.deactivate());
    }

    public List<ThemeInfo> getThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeInfo::from)
                .toList();
    }

    public List<ThemeInfo> getWeeksTopThemes() {
        return themeRepository.findByReservationCountWithLimit(
                        LocalDate.now(clock).minusWeeks(WEEKS_BOUND),
                        LocalDate.now(clock).minusDays(DAYS_BOUND),
                        THEME_SIZE_LIMIT
                ).stream()
                .map(ThemeInfo::from)
                .toList();
    }
}
