package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.request.ThemeRequest;
import roomescape.model.Theme;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public Theme addTheme(ThemeRequest themeRequest) {
        Theme theme = new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        return themeRepository.save(theme);
    }

    @Transactional
    public void deleteTheme(long id) {
        themeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes() {
        LocalDate before = LocalDate.now().minusDays(8);
        LocalDate after = LocalDate.now().minusDays(1);
        return themeRepository.findFirst10ByDateBetweenOrderByTheme(before, after);
    }

}
