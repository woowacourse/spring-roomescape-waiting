package roomescape.theme.application.service;

import roomescape.theme.application.dto.CreateThemeServiceRequest;
import roomescape.theme.domain.Theme;

public interface ThemeCommandService {

    Theme create(CreateThemeServiceRequest createThemeServiceRequest);

    void delete(Long id);
}
