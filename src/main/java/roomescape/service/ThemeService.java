package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.controller.request.ThemeRequest;
import roomescape.model.Theme;
import roomescape.repository.ThemeRepository;

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

    public void deleteTheme(long id) {
        themeRepository.deleteById(id);
    }

    public List<Theme> findPopularThemes(int count) {
        LocalDate before = LocalDate.now().minusDays(8);
        LocalDate after = LocalDate.now().minusDays(1);
        List<Theme> themes = themeRepository.findByDateBetweenOrderByTheme(before, after);
        if (themes.size() < count) {
            count = themes.size();
        }
        return themes.subList(0, count);
    }

}
