package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.command.ThemeCreateCommand;
import roomescape.service.dto.result.ThemeResult;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    private static final int DATA_RANGE = 7;

    public ThemeResult create(final ThemeCreateCommand request) {
        final Theme themeWithoutId = Theme.create(
                request.name(),
                request.description(),
                request.thumbnailUrl()
        );

        Theme theme = themeRepository.save(themeWithoutId);

        return mapDomainToDto(theme);
    }

    public void delete(final Long themeId) {
        final boolean hasAnyOngoingReservation = reservationRepository.existsByThemeId(themeId);
        if (hasAnyOngoingReservation) {
            throw new BusinessException(ErrorCode.THEME_HAS_RESERVATION);
        }

        boolean deleted = themeRepository.deleteById(themeId);

        if (!deleted) {
            throw new BusinessException(ErrorCode.THEME_NOT_FOUND);
        }
    }

    public List<ThemeResult> getPopularThemes() {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(DATA_RANGE);

        return themeRepository.findPopularThemes(startDate, today)
                .stream()
                .map(ThemeService::mapDomainToDto)
                .toList();
    }

    public List<ThemeResult> getThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeService::mapDomainToDto)
                .toList();
    }

    private static ThemeResult mapDomainToDto(Theme theme) {
        return new ThemeResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }
}
