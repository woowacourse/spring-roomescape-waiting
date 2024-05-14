package roomescape.reservation.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

class ReservationTest {

    private static final LocalTime TIME = LocalTime.of(9, 0);
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate BEFORE = LocalDate.now().minusDays(1);

    @Test
    @DisplayName("전달 받은 데이터로 Reservation 객체를 정상적으로 생성한다.")
    void constructReservation() {
        Theme theme = Theme.themeOf(1, "미르", "미르 방탈출", "썸네일 Url");
        Time time = new Time(1, TIME);
        Member member = Member.memberOf(1, "polla", "polla@gmail.com", "polla99", "admin");
        Reservation reservation = Reservation.reservationOf(1L, TOMORROW, time, theme, member);

        assertAll(
                () -> assertEquals(reservation.getTheme(), theme),
                () -> assertEquals(reservation.getReservationTime(), time),
                () -> assertEquals(reservation.getId(), 1),
                () -> assertEquals(reservation.getMember().getName(), "polla"),
                () -> assertEquals(reservation.getDate(), TOMORROW)
        );
    }

    @Test
    @DisplayName("과거의 날짜를 예약하려고 시도하는 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenReservationDateIsPast() {
        Throwable pastDateReservation = assertThrows(RoomEscapeException.class,
                () -> Reservation.saveReservationOf(BEFORE, 1L, 1L, 1L));

        assertEquals(ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION.getMessage(),
                pastDateReservation.getMessage());
    }
}
