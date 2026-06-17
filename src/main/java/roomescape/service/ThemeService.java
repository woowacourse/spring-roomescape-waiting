package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateThemeCommand;
import roomescape.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private static final int POPULAR_THEME_PERIOD_DAYS = 6;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ThemeResponse createTheme(CreateThemeCommand command) {
        validateUniqueTheme(command.name());

        Theme theme = Theme.createWithoutId(command.name(), command.description(), command.thumbnail());
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> getPopularThemes(LocalDate today) {
        LocalDate startDate = today.minusDays(POPULAR_THEME_PERIOD_DAYS);
        LocalDate endDate = today.minusDays(1);

        List<Theme> popularThemes = themeRepository.findPopularThemesByPeriod(startDate, endDate);
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTheme(long themeId) {
        Optional<Theme> theme = themeRepository.findById(themeId);
        if (theme.isEmpty()) {
            throw new RoomEscapeException(ThemeErrorCode.NOT_FOUND);
        }

        validateThemeIncludeReservation(themeId);
        themeRepository.deleteById(themeId);
    }

    private void validateUniqueTheme(String name) {
        boolean exists = themeRepository.existsByName(name);
        if (exists) {
            throw new RoomEscapeException(ThemeErrorCode.DUPLICATE);
        }
    }

    private void validateThemeIncludeReservation(long themeId) {
        boolean existsByThemeId = reservationRepository.existsByThemeId(themeId);
        if (existsByThemeId) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_CANNOT_DELETE);
        }
    }
}
