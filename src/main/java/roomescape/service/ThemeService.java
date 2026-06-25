package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.ThemePatchRequest;
import roomescape.controller.dto.ThemeRequest;
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
    public Theme saveTheme(ThemeRequest request) {
        Theme theme = Theme.transientOf(request.name(), request.description(), request.thumbnailUrl());
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
    public Theme putTheme(long id, ThemeRequest request) {
        findThemeById(id);
        return themeRepository.update(new Theme(id, request.name(), request.description(), request.thumbnailUrl()));
    }

    @Transactional
    public Theme patchTheme(long id, ThemePatchRequest request) {
        Theme theme = findThemeById(id);
        Theme renewed = theme.renewal(request.name(), request.description(), request.thumbnailUrl());
        return themeRepository.update(renewed);
    }

    public List<Theme> findPopularThemes(Long topCount, Long during) {
        LocalDate toDate = LocalDate.now().minusDays(ONE_DAY);
        LocalDate fromDate = LocalDate.now().minusDays(during);
        return themeRepository.findPopularThemes(topCount, fromDate, toDate);
    }
}
