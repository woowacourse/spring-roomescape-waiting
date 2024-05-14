package roomescape.service.theme;

import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeSaveRequest;

@Service
public class ThemeCreateService {

    private final ThemeRepository themeRepository;

    public ThemeCreateService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme createTheme(ThemeSaveRequest request) {
        Theme theme = request.toEntity(request);
        return themeRepository.save(theme);
    }
}
