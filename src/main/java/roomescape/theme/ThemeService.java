package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.theme.ThemeNotFoundException;
import roomescape.exception.custom.reason.theme.ThemeUsedException;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@Service
public class ThemeService {

    private static final int BETWEEN_DAY_START = 7;
    private static final int BETWEEN_DAY_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ThemeService(
            final ThemeRepository themeRepository,
            final ReservationRepository reservationRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse create(
            final ThemeRequest request
    ) {
        final Theme notSavedTheme = new Theme(
                request.name(),
                request.description(),
                request.thumbnail()
        );

        final long id = themeRepository.save(notSavedTheme);
        final Theme savedTheme = themeRepository.findById(id);
        return ThemeResponse.from(savedTheme);
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
        validateExistsTheme(id);
        validateUnusedTheme(id);

        themeRepository.deleteById(id);
    }

    private void validateExistsTheme(final Long id) {
        if (!themeRepository.existsById(id)) {
            throw new ThemeNotFoundException();
        }
    }

    private void validateUnusedTheme(final Long id) {
        if (reservationRepository.existsByTheme(id)) {
            throw new ThemeUsedException();
        }
    }
}
