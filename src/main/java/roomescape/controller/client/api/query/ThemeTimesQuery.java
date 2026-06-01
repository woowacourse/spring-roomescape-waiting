package roomescape.controller.client.api.query;

import java.time.LocalDate;
import java.util.List;
import roomescape.controller.client.api.dto.response.ThemeTimesResponse;

public interface ThemeTimesQuery {

    List<ThemeTimesResponse> getThemeReservationStatus(long themeId, LocalDate date);
}
