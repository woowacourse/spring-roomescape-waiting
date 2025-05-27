package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.exception.ThemeNotFoundException;
import roomescape.reservation.application.exception.UsingThemeException;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.PopularThemeResponse;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.ThemeResponse;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse create(ThemeRequest request) {

        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        return ThemeResponse.from(themeRepository.save(theme));
    }

    public List<ThemeResponse> getAll() {

        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    public void deleteById(Long id) {

        Theme theme = getTheme(id);

        if (reservationRepository.existsByTheme(theme)) {
            throw new UsingThemeException();
        }

        themeRepository.delete(theme);
    }

    public List<PopularThemeResponse> getPopularThemes() {

        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(7);
        List<Long> themeIds = reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate).stream()
                .limit(10)
                .map(Theme::getId)
                .toList();

        return themeIds.stream().map(themeId -> {
            Theme theme = themeRepository.findById(themeId)
                    .orElseThrow(() -> new ThemeNotFoundException(themeId));
            return PopularThemeResponse.from(theme);
        }).toList();
    }

    private Theme getTheme(Long themeId) {
        
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));
    }
}
