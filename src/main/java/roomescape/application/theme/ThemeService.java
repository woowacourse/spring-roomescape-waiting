package roomescape.application.theme;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.theme.request.CreateThemeRequest;
import roomescape.application.theme.response.AdminThemesResponse;
import roomescape.application.theme.response.CreateThemeResponse;
import roomescape.application.theme.response.PopularThemesResponse;
import roomescape.application.theme.response.ThemesResponse;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRankResult;
import roomescape.domain.theme.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_LIMIT = 7;

    private final ThemeRepository themeRepository;
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
        try {
            int deletedRow = themeRepository.deleteById(id);
            if (deletedRow == 0) {
                throw new BusinessException(ErrorCode.THEME_NOT_FOUND);
            }
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.THEME_IN_USE);
        }
    }
}
