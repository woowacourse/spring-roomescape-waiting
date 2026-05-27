package roomescape.service.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.service.command.ReservationChangeCommand;
import roomescape.service.result.ReservationEntryResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeResult;

public class ReservationServiceFixture {

    public static ReservationResult createReservationResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, LocalTime.now(), "ACTIVE");
        ThemeResult themeResult = new ThemeResult(1L, "테마", "테마설명", "테마 이미지", true);
        ReservationEntryResult entryResult = new ReservationEntryResult(1L, "이프", "RESERVED", LocalDateTime.now());
        return new ReservationResult(1L, LocalDate.now(), themeResult, timeResult, entryResult);
    }

    public static ReservationChangeCommand createChangeCommand(LocalDate date, long timeId) {
        return new ReservationChangeCommand(date, timeId);
    }
}
