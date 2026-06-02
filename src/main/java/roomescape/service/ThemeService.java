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

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> allTheme() {
        return themeRepository.findAll();
    }

    public Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(ThemeNotFoundException::new);
    }

    @Transactional
    public Theme saveTheme(String name, String description, String thumbnailUrl) {
        Theme theme = new Theme(name, description, thumbnailUrl);
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

    public List<Theme> findPopularThemes(Long limit, Long days) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = LocalDate.now().minusDays(days);
        return themeRepository.findPopularThemes(limit, startDate, endDate);
    }
}
