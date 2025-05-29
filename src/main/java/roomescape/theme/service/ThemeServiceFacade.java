package roomescape.theme.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@Service
@AllArgsConstructor
public class ThemeServiceFacade {

    private final ThemeService themeService;

    @Transactional
    public ThemeResponse createTheme(ThemeCreateRequest request) {
        return themeService.create(request);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeService.findAll();
    }

    @Transactional
    public void deleteThemeById(Long id) {
        themeService.deleteThemeById(id);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findPopular() {
        return themeService.findLimitedThemesByPopularDesc();
    }
}
