package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Theme;
import roomescape.exception.reservation.ThemeUsingException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.theme.PopularThemeRequest;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findTopBookedThemes(PopularThemeRequest request) {
        List<Theme> topBookedThemes = themeRepository.findPopularThemes(request.getStartDate(), request.getEndDate());
        return topBookedThemes.stream()
                .limit(request.getCount())
                .map(ThemeResponse::new)
                .toList();
    }

    public ThemeResponse createTheme(ThemeRequest request) {
        Theme theme = themeRepository.save(request.toTheme());
        return new ThemeResponse(theme);
    }

    public void deleteTheme(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ThemeUsingException();
        }
        themeRepository.deleteById(id);
    }
}
