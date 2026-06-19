package roomescape.theme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.repository.projection.PopularThemeResult;

import java.time.LocalDate;
import java.util.List;

import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_STATUS_UPDATE_FAILED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ThemeService {

    private static final int POPULAR_STATISTICS_DURATION = 7;
    private static final int PREVIOUS_DAYS = 1;

    private final ThemeRepository themeRepository;

    public Theme readTheme(Long id) {
        return getTheme(id);
    }

    public List<Theme> readThemes() {
        return themeRepository.findAll();
    }

    // TODO 더이상 필요하지 않으니 삭제.
    public List<Theme> readActiveThemes() {
        return themeRepository.findByIsActive(true);
    }

    public List<Theme> readSlotOfThemes() {
        return themeRepository.findSlotOfThemes();
    }

    public List<PopularThemeResult> readPopularThemes(int top) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(PREVIOUS_DAYS);
        LocalDate startDate = today.minusDays(PREVIOUS_DAYS + POPULAR_STATISTICS_DURATION);
        return themeRepository.findPopularThemes(startDate, endDate, top);
    }

    @Transactional
    public Theme register(String name, String description, String thumbnailUrl, Long amount) {
        return themeRepository.save(Theme.create(name, description, thumbnailUrl, amount));
    }

    @Transactional
    public Theme updateStatus(Long id, boolean isActive) {
        Theme theme = getTheme(id);
        theme.updateStatus(isActive);
        boolean isUpdated = themeRepository.updateStatus(theme);
        validateIsUpdated(isUpdated);
        return theme;
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ThemeException(THEME_NOT_FOUND));
    }

    private void validateIsUpdated(boolean isUpdated) {
        if (!isUpdated) {
            throw new ThemeException(THEME_STATUS_UPDATE_FAILED);
        }
    }

}
