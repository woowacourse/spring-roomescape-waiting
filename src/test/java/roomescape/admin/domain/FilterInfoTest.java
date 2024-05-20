package roomescape.admin.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.model.RoomEscapeException;
import roomescape.reservation.exception.ReservationExceptionCode;

class FilterInfoTest {

    private static final LocalDate fromDate = LocalDate.now();
    private static final LocalDate toDate = LocalDate.now().plusDays(1);

    @Test
    @DisplayName("멤버의 정보가 없을 경우 예외를 던진다.")
    void validation_shouldThrowException_whenMemberInfoNull() {
        Throwable memberInfoNull = assertThrows(RoomEscapeException.class,
                () -> new FilterInfo(null, 1L, fromDate, toDate));

        assertEquals(memberInfoNull.getMessage(), ReservationExceptionCode.MEMBER_INFO_IS_NULL_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("테마의 정보가 없을 경우 예외를 던진다.")
    void validation_shouldThrowException_whenThemeInfoNull() {
        Throwable themeInfoNull = assertThrows(RoomEscapeException.class,
                () -> new FilterInfo(1L, null, fromDate, toDate));

        assertEquals(themeInfoNull.getMessage(), ReservationExceptionCode.THEME_INFO_IS_NULL_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("선택한 날짜가 없을 경우 예외를 던진다.")
    void validation_shouldThrowException_whenFromDateInfoNull() {
        Throwable fromDateInfoNull = assertThrows(RoomEscapeException.class,
                () -> new FilterInfo(1L, 1L, null, toDate));

        assertEquals(fromDateInfoNull.getMessage(), ReservationExceptionCode.DATE_IS_NULL_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("선택한 날짜가 없을 경우 예외를 던진다.")
    void validation_shouldThrowException_whenToDateInfoNull() {
        Throwable toDateInfoNull = assertThrows(RoomEscapeException.class,
                () -> new FilterInfo(1L, 1L, fromDate, null));

        assertEquals(toDateInfoNull.getMessage(), ReservationExceptionCode.DATE_IS_NULL_EXCEPTION.getMessage());
    }
}
