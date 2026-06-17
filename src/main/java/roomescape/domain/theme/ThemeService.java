package roomescape.domain.theme;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.domain.theme.admin.dto.AdminThemeResponse;
import roomescape.domain.theme.admin.dto.CreateThemeRequest;
import roomescape.domain.theme.admin.dto.CreateThemeResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeRankResult;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_LIMIT = 7;

    private final JpaThemeRepository themeRepository;
    private final JpaReservationSlotRepository reservationSlotRepository;
    private final ThemeRepository themeQueryRepository;

    private final Clock clock;

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
        if (reservationSlotRepository.countByThemeId(id) > 0) {
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
        LocalDate today = LocalDate.now(clock);
        LocalDate startDay = today.minusDays(RANK_DAYS_LIMIT);
        List<ThemeRankResult> popularThemes = themeQueryRepository.findPopularThemes(RANK_LIMIT, startDay, today);
        return popularThemes.stream()
            .map(ThemeRankResponse::from)
            .toList();
    }

    public Theme findThemeByIdOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
    }
}
