package roomescape.service.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.theme.PopularThemeResponse;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.theme.ThemeRepository;

@Service
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeServiceImpl(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public ThemeResponse create(ThemeRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        return ThemeResponse.from(themeRepository.save(theme));
    }

    @Override
    public List<ThemeResponse> getAll() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    @Override
    public void deleteById(Long id) {
        themeRepository.deleteById(id);
    }

    @Override
    public List<PopularThemeResponse> getPopularThemes() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(7);
        List<Long> themeIds = reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate);

        return themeIds.stream().map(themeId -> {
            Theme theme = themeRepository.findById(themeId)
                    .orElseThrow(() -> new ThemeNotFoundException(themeId));
            return PopularThemeResponse.from(theme);
        }).toList();
    }
}
