package roomescape.service.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.service.command.ReservationChangeCommand;
import roomescape.service.command.ReservationCommand;
import roomescape.service.result.ReservationEntryResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeRegisterResult;

public class ReservationServiceFixture {

    public static ReservationResult createReservationResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, LocalTime.now(), "ACTIVE");
        ThemeRegisterResult themeResult = new ThemeRegisterResult(1L, "테마", "테마설명", "테마 이미지", 30000L, true);
        ReservationEntryResult entryResult = new ReservationEntryResult(1L, "이프", "RESERVED", LocalDateTime.now());
        return new ReservationResult(1L, LocalDate.now(), themeResult, timeResult, entryResult);
    }

    public static ReservationResult createWaitingResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, LocalTime.now(), "ACTIVE");
        ThemeRegisterResult themeResult = new ThemeRegisterResult(1L, "테마", "테마설명", "테마 이미지", 30000L, true);
        ReservationEntryResult entryResult = new ReservationEntryResult(2L, "라텔", "WAITING", LocalDateTime.now());
        return new ReservationResult(1L, LocalDate.now(), themeResult, timeResult, entryResult);
    }

    public static ReservationChangeCommand createChangeCommand(LocalDate date, long timeId) {
        return new ReservationChangeCommand(date, timeId);
    }

    public static final long DEFAULT_AMOUNT = 30000L;

    public static ReservationCommand createReserveCommand(String name, LocalDate date) {
        return createReserveCommand(name, date, 1L, 1L);
    }

    public static ReservationCommand createReserveCommand(String name, LocalDate date, long themeId, long timeId) {
        return new ReservationCommand(name, date, themeId, timeId, DEFAULT_AMOUNT);
    }
}
