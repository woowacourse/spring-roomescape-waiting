package roomescape.theme.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@Service
@AllArgsConstructor
public class ThemeServiceFacade {

    private final ThemeService themeService;

    public ThemeResponse createTheme(ThemeCreateRequest request) {
        return themeService.create(request);
    }

    public List<ThemeResponse> findAll() {
        return themeService.findAll();
    }

    public void deleteThemeById(Long id) {
        themeService.deleteThemeById(id);
    }

    public List<ThemeResponse> findPopular() {
        return themeService.findLimitedThemesByPopularDesc();
    }
}
