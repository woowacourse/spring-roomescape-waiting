package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.admin.dto.AdminThemeResponse;
import roomescape.domain.theme.admin.dto.CreateThemeRequest;
import roomescape.domain.theme.admin.dto.CreateThemeResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_LIMIT = 7;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public List<AdminThemeResponse> getAllThemeForAdmin() {
        return themeRepository.findAll().stream()
            .map(AdminThemeResponse::from)
            .toList();
    }

    public CreateThemeResponse createTheme(CreateThemeRequest request) {
        Theme theme = themeRepository.save(request.toEntity());
        return CreateThemeResponse.from(theme);
    }

    public void deleteTheme(Long id) {
        if (reservationRepository.countByThemeId(id) > 0) {
            throw new ConflictException(ThemeErrors.THEME_IN_USE);
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getAllTheme() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }

    public List<ThemeRankResponse> getThemeRank() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(RANK_DAYS_LIMIT);
        List<Theme> populateThemes = reservationRepository.findPopularThemes(RANK_LIMIT, startDay, today);
        return populateThemes.stream()
            .map(ThemeRankResponse::from)
            .toList();
    }
}
