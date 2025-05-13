package roomescape.theme.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@RequiredArgsConstructor
@Service
public class ThemeService {

    private static final int POPULAR_THEME_DAYS = 7;
    private static final int POPULAR_THEME_LIMIT = 10;

    private final ThemeRepository themeRepository;

    public Long save(final String name, final String description, final String thumbnail) {
        if (themeRepository.existsByName(name)) {
            throw new DataExistException("해당 테마명이 이미 존재합니다. name = " + name);
        }

        final Theme theme = new Theme(name, description, thumbnail);

        return themeRepository.save(theme);
    }

    public void deleteById(final Long id) {
        themeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + id));

        themeRepository.deleteById(id);
    }

    public Theme getById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + id));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findPopularThemes() {
        return themeRepository.findTop10ThemesByReservationCountWithin7Days(POPULAR_THEME_DAYS, POPULAR_THEME_LIMIT);
    }
}
