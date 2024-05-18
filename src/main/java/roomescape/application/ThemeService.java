package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ThemeRequest;
import roomescape.application.dto.ThemeResponse;
import roomescape.domain.PopularThemeFinder;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeCommandRepository;
import roomescape.domain.repository.ThemeQueryRepository;

@Service
public class ThemeService {

    private final ThemeCommandRepository themeCommandRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final PopularThemeFinder popularThemeFinder;

    public ThemeService(ThemeCommandRepository themeCommandRepository, ThemeQueryRepository themeQueryRepository, PopularThemeFinder popularThemeFinder) {
        this.themeCommandRepository = themeCommandRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.popularThemeFinder = popularThemeFinder;
    }

    public ThemeResponse create(ThemeRequest request) {
        Theme savedTheme = themeCommandRepository.save(request.toTheme());
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAll() {
        return themeQueryRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        Theme theme = themeQueryRepository.getById(id);
        themeCommandRepository.deleteById(theme.getId());
    }


    public List<ThemeResponse> findPopularThemes() {
        return popularThemeFinder.findThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
