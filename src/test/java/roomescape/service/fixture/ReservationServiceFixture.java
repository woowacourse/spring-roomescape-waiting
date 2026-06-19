package roomescape.service.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.application.service.command.ReservationChangeCommand;
import roomescape.application.service.result.ReservationResult;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.application.service.result.ReservationTimeResult;
import roomescape.application.service.result.ThemeRegisterResult;

public class ReservationServiceFixture {

    public static ReservationSlotResult createReservationSlotResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, LocalTime.now(), "ACTIVE");
        ThemeRegisterResult themeResult = new ThemeRegisterResult(1L, "테마", "테마설명", "테마 이미지");
        ReservationResult reservationResult = new ReservationResult(1L, "이프", "RESERVED", LocalDateTime.now());
        return new ReservationSlotResult(1L, LocalDate.now(), themeResult, timeResult, reservationResult);
    }

    public static ReservationSlotResult createWaitingResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, LocalTime.now(), "ACTIVE");
        ThemeRegisterResult themeResult = new ThemeRegisterResult(1L, "테마", "테마설명", "테마 이미지");
        ReservationResult reservationResult = new ReservationResult(2L, "라텔", "WAITING", LocalDateTime.now());
        return new ReservationSlotResult(1L, LocalDate.now(), themeResult, timeResult, reservationResult);
    }

    public static ReservationChangeCommand createChangeCommand(LocalDate date, long timeId) {
        return new ReservationChangeCommand(date, timeId);
    }
}
