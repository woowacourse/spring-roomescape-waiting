package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.ResourceInUseException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private static final int ONE_DAY = 1;
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> allTheme() {
        return themeRepository.findAll();
    }

    public Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ThemeNotFoundException(id));
    }

    @Transactional
    public Theme saveTheme(String name, String description, String thumbnailUrl) {
        Theme theme = Theme.transientOf(name, description, thumbnailUrl);
        return themeRepository.save(theme);
    }

    @Transactional
    public void removeTheme(long themeId) {
        try {
            findThemeById(themeId);
            themeRepository.deleteById(themeId);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("테마");
        }
    }

    @Transactional
    public void putTheme(long id, String name, String description, String thumbnailUrl) {
        findThemeById(id);
        themeRepository.update(new Theme(id, name, description, thumbnailUrl));
    }

    @Transactional
    public void patchTheme(long id, String name, String description, String thumbnailUrl) {
        Theme theme = findThemeById(id);
        theme.renewal(name, description, thumbnailUrl);
        themeRepository.update(theme);
    }

    public List<Theme> findPopularThemes(Long topCount, Long during) {
        LocalDate toDate = LocalDate.now().minusDays(ONE_DAY);
        LocalDate fromDate = LocalDate.now().minusDays(during);
        return themeRepository.findPopularThemes(topCount, fromDate, toDate);
    }
}
