package roomescape.controller.client.api.query;

import java.time.LocalDate;
import java.util.List;
import roomescape.controller.client.api.dto.response.ThemeResponse;

public interface ThemeQuery {

    List<ThemeResponse> getAllActiveThemes();

    List<ThemeResponse> getPopularThemes(LocalDate startDate, LocalDate endDate);
}
