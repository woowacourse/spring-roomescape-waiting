package roomescape.service.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeResult;

public class ReservationServiceFixture {

    public static ReservationResult createReservationResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, LocalTime.now(), "ACTIVE");
        ThemeResult themeResult = new ThemeResult(1L, "테마", "테마설명", "테마 이미지", true);
        return new ReservationResult(1L, "이프", LocalDate.now(), themeResult, timeResult);
    }
}
