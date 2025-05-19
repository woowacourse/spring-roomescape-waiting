package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.theme.ThemeNotFoundException;
import roomescape.exception.custom.reason.theme.ThemeUsedException;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@Service
@AllArgsConstructor
public class ThemeService {

    private static final int BETWEEN_DAY_START = 7;
    private static final int BETWEEN_DAY_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

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

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findTopRankThemes(final int size) {
        final LocalDate now = LocalDate.now();
        final LocalDate from = now.minusDays(BETWEEN_DAY_START);
        final LocalDate to = now.minusDays(BETWEEN_DAY_END);
        return themeRepository.findAllOrderByRank(from, to, size).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteById(
            final Long id
    ) {
        final Theme theme = themeRepository.findById(id)
                .orElseThrow(ThemeNotFoundException::new);

        if (reservationRepository.existsByTheme(theme)) {
            throw new ThemeUsedException();
        }

        themeRepository.delete(theme);
    }
}
