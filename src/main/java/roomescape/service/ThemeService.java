package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.dto.request.ThemeRequest;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.JpaThemeRepository;

@Service
public class ThemeService {

    public static final int TOP_RANK_PERIOD_DAYS = 7;

    private final JpaThemeRepository themeRepository;

    public ThemeService(JpaThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public List<Theme> findTopReservedThemes() {
        LocalDate today = LocalDate.now();

        return themeRepository.findTop10ByDateBetween(today.minusDays(TOP_RANK_PERIOD_DAYS), today);
    }

    public Theme addTheme(ThemeRequest request) {
        validateDuplicateTheme(request);

        return themeRepository.save(
            new Theme(request.name(), request.description(), request.thumbnail()));
    }

    private void validateDuplicateTheme(ThemeRequest request) {
        if (themeRepository.existsByName(request.name())) {
            throw new DuplicatedException("theme");
        }
    }

    public void removeTheme(Long id) {
        themeRepository.deleteById(id);
    }
}
