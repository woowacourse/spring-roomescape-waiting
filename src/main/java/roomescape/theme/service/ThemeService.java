package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.controller.dto.request.ThemeRequest;
import roomescape.theme.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.NotFoundException;
import roomescape.theme.repository.JpaThemeRepository;

@Service
public class ThemeService {

    public static final int TOP_RANK_PERIOD_DAYS = 7;

    private final JpaThemeRepository themeRepository;

    public ThemeService(final JpaThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Theme> findTopReservedThemes() {
        LocalDate today = LocalDate.now();

        return themeRepository.findTop10ByDateBetween(today.minusDays(TOP_RANK_PERIOD_DAYS), today);
    }

    @Transactional
    public Theme addTheme(ThemeRequest request) {
        validateDuplicateTheme(request);

        return themeRepository.save(
            new Theme(request.name(), request.description(), request.thumbnail()));
    }

    @Transactional(readOnly = true)
    private void validateDuplicateTheme(ThemeRequest request) {
        if (themeRepository.existsByName(request.name())) {
            throw new DuplicatedException("theme");
        }
    }

    @Transactional
    public void removeTheme(final long id) {
        if (!themeRepository.existsById(id)) {
            throw new NotFoundException("theme");
        }
        themeRepository.deleteById(id);
    }
}
