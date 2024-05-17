package roomescape.domain;

import java.time.LocalDate;
import roomescape.application.dto.ReservationRequest;

class ReservationRequestFixture {

    static ReservationRequest of(long timeId, long themeId) {
        return new ReservationRequest(LocalDate.of(2024, 1, 1), timeId, themeId);
    }

    static ReservationRequest of(LocalDate date, long timeId, long themeId) {
        return new ReservationRequest(date, timeId, themeId);
    }
}
