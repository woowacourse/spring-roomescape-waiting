package roomescape.domain.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddRequest;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.exception.NoMatchingDataException;

@Service
public class ThemeService {

    protected static final String NON_EXIST_THEME_ID_ERROR_MESSAGE = "해당 id를 가진 테마가 존재하지 않습니다.";

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAllTheme() {
        return themeRepository.findAll();
    }

    public Theme addTheme(ThemeAddRequest themeAddRequest) {
        Theme theme = themeAddRequest.toEntity();
        return themeRepository.save(theme);
    }

    public void removeTheme(Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NoMatchingDataException(NON_EXIST_THEME_ID_ERROR_MESSAGE);
        }
        themeRepository.deleteById(id);
    }

    public List<Theme> getThemeRanking() {
        return themeRepository.findThemeOrderByReservationCount();
    }
}
