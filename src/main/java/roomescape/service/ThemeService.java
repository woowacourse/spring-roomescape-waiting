package roomescape.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;
import roomescape.dto.theme.ThemeResponse;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Transactional
@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(final ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ThemeResponse create(final Theme theme) {
        return ThemeResponse.from(themeRepository.save(theme));
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        final List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ThemeResponse findById(final Long id) {
        final Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "에 해당하는 테마가 없습니다."));
        return ThemeResponse.from(theme);
    }

    public void deleteById(final Long id) {
        final Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "에 해당하는 테마가 없습니다."));
        themeRepository.deleteById(theme.getId());
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findPopularThemes() {
        final ThemePopularFilter filter = ThemePopularFilter.from(LocalDate.now());
        Pageable size = filter.ofSize();

        final List<Theme> popularThemes = themeRepository.findPopularThemesBy(filter, size);
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
