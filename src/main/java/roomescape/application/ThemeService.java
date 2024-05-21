package roomescape.application;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.PopularThemeFinder;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final PopularThemeFinder popularThemeFinder;

    public ThemeService(ThemeRepository themeRepository, PopularThemeFinder popularThemeFinder) {
        this.themeRepository = themeRepository;
        this.popularThemeFinder = popularThemeFinder;
    }

    public ThemeResponse save(ThemeRequest themeRequest) {
        Theme savedTheme = themeRepository.save(themeRequest.toTheme());
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
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND,
                        String.format("존재하지 않는 테마입니다. 요청 테마 id:%d", id)));
        themeRepository.deleteById(theme.getId());
    }


    public List<ThemeResponse> findPopularThemes() {
        return popularThemeFinder.findThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
