package roomescape.theme;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.booking.reservation.ReservationService;
import roomescape.exception.custom.reason.theme.ThemeNotFoundException;
import roomescape.exception.custom.reason.theme.ThemeUsedException;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ThemeService {

    private static final int BETWEEN_DAY_START = 7;
    private static final int BETWEEN_DAY_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationService reservationService;

    @Transactional
    public ThemeResponse create(
            final ThemeRequest request
    ) {
        final Theme notSavedTheme = new Theme(
                request.name(),
                request.description(),
                request.thumbnail()
        );

        final Theme theme = themeRepository.save(notSavedTheme);
        return ThemeResponse.from(theme);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Theme findById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(ThemeNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findTopRankThemes(final int size) {
        final LocalDate now = LocalDate.now();
        final LocalDate from = now.minusDays(BETWEEN_DAY_START);
        final LocalDate to = now.minusDays(BETWEEN_DAY_END);
        return themeRepository.findAllOrderByRank(from, to, size).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(
            final Long id
    ) {
        final Theme theme = findById(id);
        if (reservationService.existsByTheme(theme)) {
            throw new ThemeUsedException();
        }

        themeRepository.delete(theme);
    }
}
