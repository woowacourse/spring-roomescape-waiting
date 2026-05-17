package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundException;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private static final String NOT_FOUND_THEME = "존재하지 않는 테마입니다. (ID: %d)";

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> getThemes() {
        return themeRepository.findAll();
    }

    @Transactional
    public Theme addTheme(Theme theme) {
        return themeRepository.save(theme);
    }

    @Transactional
    public void deleteTheme(Long id) {
        themeRepository.deleteById(id);
    }

    public Theme findById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_THEME, id)));
    }

    public List<Theme> getPopularThemes(LocalDate now, Integer days, Integer limit) {
        LocalDate start = now.minusDays(days);
        LocalDate end = now.minusDays(1);
        return themeRepository.getPopularThemes(start, end, limit);
    }
}
