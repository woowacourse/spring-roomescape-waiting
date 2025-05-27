package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedThemeException;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ThemeRequest;
import roomescape.reservation.dto.response.ThemeResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private static final int TOP_THEMES_COUNT = 10;
    private static final int DAYS_BEFORE_START = 7;
    private static final int DAYS_BEFORE_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ThemeResponse createTheme(ThemeRequest themeRequest) {
        Optional<Theme> optionalTheme = themeRepository.findByName(themeRequest.name());
        if (optionalTheme.isPresent()) {
            throw new ExistedThemeException();
        }

        Theme theme = themeRepository.save(themeRequest.toTheme());
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteThemeById(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ExistedReservationException();
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getTopThemes() {
        LocalDate startDate = LocalDate.now().minusDays(DAYS_BEFORE_START);
        LocalDate endDate = LocalDate.now().minusDays(DAYS_BEFORE_END);
        Pageable page = PageRequest.ofSize(10);
        return themeRepository.findByDateBetweenOrderByReservationCountDescNameAsc(startDate, endDate, page);
    }
}
