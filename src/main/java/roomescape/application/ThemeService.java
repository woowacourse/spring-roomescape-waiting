package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ThemeRequest;
import roomescape.application.dto.ThemeResponse;
import roomescape.domain.PopularThemeFinder;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final PopularThemeFinder popularThemeFinder;

    public ThemeService(ThemeRepository themeRepository, PopularThemeFinder popularThemeFinder) {
        this.themeRepository = themeRepository;
        this.popularThemeFinder = popularThemeFinder;
    }

    public ThemeResponse create(ThemeRequest request) {
        Theme savedTheme = themeRepository.create(request.toTheme());
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_THEME,
                        String.format("존재하지 않는 테마입니다. 요청 테마 id:%d", id)));
        themeRepository.deleteById(theme.getId());
    }


    public List<ThemeResponse> findPopularThemes() {
        return popularThemeFinder.findThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
