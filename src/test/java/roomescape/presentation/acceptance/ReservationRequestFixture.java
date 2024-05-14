package roomescape.presentation.acceptance;

import java.time.LocalDate;
import roomescape.application.dto.ReservationRequest;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

class ReservationRequestFixture {

    static ReservationRequest defaultValue(ReservationTime time, Theme theme) {
        return of(time.getId(), theme.getId());
    }

    static ReservationRequest of(long timeId, long themeId) {
        return new ReservationRequest(LocalDate.of(2024,1,1), timeId, themeId);
    }

    static ReservationRequest of(LocalDate date, long timeId, long themeId) {
        return new ReservationRequest(date, timeId, themeId);
    }
}
