package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.dto.business.ThemeCreationContent;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository repository;

    public ThemeService(ThemeRepository repository) {
        this.repository = repository;
    }

    public List<ThemeResponse> findAllThemes() {
        return repository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findTopThemes(LocalDate from, LocalDate to, int size) {
        List<Theme> themes = repository.findThemesOrderByReservationCount(from, to, size);
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public ThemeResponse addTheme(ThemeCreationContent request) {
        Theme theme = Theme.createWithoutId(request.name(), request.description(), request.thumbnail());
        Theme savedTheme = repository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    public void deleteThemeById(Long id) {
        repository.deleteById(id);
    }
}
