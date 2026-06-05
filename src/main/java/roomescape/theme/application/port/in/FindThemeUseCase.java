package roomescape.theme.application.port.in;

import java.time.LocalDate;
import java.util.List;
import roomescape.theme.application.dto.response.ThemeFindResponse;

public interface FindThemeUseCase {
    List<ThemeFindResponse> findThemesBySlotDate(LocalDate date);
    List<ThemeFindResponse> findPopularTheme();
    List<ThemeFindResponse> findAll();
}
