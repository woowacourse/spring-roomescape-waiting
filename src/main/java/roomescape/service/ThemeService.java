package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.dto.request.ThemeCreateRequest;
import roomescape.dto.response.ThemeResult;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    private static final int DATA_RANGE = 7;

    public ThemeResult create(final ThemeCreateRequest command) {
        final Theme themeWithoutId = Theme.create(
                command.name(),
                command.description(),
                command.thumbnailUrl()
        );

        final Theme theme = themeRepository.save(themeWithoutId);

        return ThemeResult.from(theme);
    }

    public List<ThemeResult> getPopularThemes() {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(DATA_RANGE);

        return themeRepository.findPopularThemes(startDate, today)
                .stream()
                .map(ThemeResult::from)
                .toList();
    }

    public List<ThemeResult> getThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResult::from)
                .toList();
    }

    public void delete(final Long themeId) {
        final boolean hasAnyOngoingReservation = reservationRepository.existsByThemeId(themeId);
        if (hasAnyOngoingReservation) {
            throw new BusinessException(ErrorCode.THEME_HAS_RESERVATION);
        }

        final boolean deleted = themeRepository.deleteById(themeId);

        if (!deleted) {
            throw new BusinessException(ErrorCode.THEME_NOT_FOUND);
        }
    }
}
