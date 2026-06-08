package roomescape.theme.application.port.in;

import roomescape.theme.application.dto.request.ThemeSaveRequest;
import roomescape.theme.application.dto.response.ThemeSaveResponse;

public interface CreateThemeUseCase {
    ThemeSaveResponse save(ThemeSaveRequest body);
}
