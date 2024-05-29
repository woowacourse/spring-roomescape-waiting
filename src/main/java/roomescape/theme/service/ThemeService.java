package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.exception.model.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme addTheme(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toTheme();
        return themeRepository.save(theme);
    }

    public List<Theme> findThemes() {
        return themeRepository.findAll();
    }

    public Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(ThemeNotFoundException::new);
    }

    public void removeTheme(Long id) {
        themeRepository.deleteById(id);
    }
}
