package roomescape.application.theme;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRankResult;
import roomescape.domain.theme.ThemeRepository;
import roomescape.presentation.theme.request.CreateThemeRequest;
import roomescape.presentation.theme.response.AdminThemesResponse;
import roomescape.presentation.theme.response.CreateThemeResponse;
import roomescape.presentation.theme.response.PopularThemesResponse;
import roomescape.presentation.theme.response.ThemesResponse;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_LIMIT = 7;

    private final ThemeRepository themeRepository;
    private final ReservationSlotRepository slotRepository;
    private final Clock clock;

    public ThemesResponse getAllTheme() {
        return ThemesResponse.from(themeRepository.findAll());
    }

    public AdminThemesResponse getAllThemeForAdmin() {
        return AdminThemesResponse.from(themeRepository.findAll());
    }

    public PopularThemesResponse getThemeRank() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDay = today.minusDays(RANK_DAYS_LIMIT);

        List<ThemeRankResult> popularThemes = themeRepository.findPopularThemes(RANK_LIMIT, startDay, today);
        return PopularThemesResponse.from(popularThemes);
    }

    @Transactional
    public CreateThemeResponse createTheme(CreateThemeRequest request) {
        Theme theme = Theme.create(request.name(), request.content(), request.thumbnailUrl());
        Theme savedTheme = themeRepository.save(theme);
        return CreateThemeResponse.from(savedTheme);
    }

    @Transactional
    public void deleteTheme(Long id) {
        if (slotRepository.existsByThemeId(id)) {
            throw new BusinessException(ErrorCode.THEME_IN_USE);
        }
        int deletedRow = themeRepository.deleteById(id);
        if (deletedRow == 0) {
            throw new BusinessException(ErrorCode.THEME_NOT_FOUND);
        }
    }
}
