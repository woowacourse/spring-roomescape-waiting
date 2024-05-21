package roomescape;

import java.time.LocalDate;
import org.springframework.boot.test.context.TestComponent;
import roomescape.application.dto.ReservationRequest;

@TestComponent
public class ReservationRequestFixture {

    public static ReservationRequest of(long timeId, long themeId) {
        return new ReservationRequest(LocalDate.of(2024, 1, 1), timeId, themeId);
    }

    public static ReservationRequest of(LocalDate date, long timeId, long themeId) {
        return new ReservationRequest(date, timeId, themeId);
    }
}
