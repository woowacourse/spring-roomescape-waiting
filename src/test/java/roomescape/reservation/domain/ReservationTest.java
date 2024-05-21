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

    private static final LocalTime NOW = LocalTime.of(9, 0);
    public static final Time TIME = Time.from(NOW);
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate BEFORE = LocalDate.now().minusDays(1);
    public static final Theme THEME = Theme.of("미르", "미르 방탈출", "썸네일 Url");
    public static final Member MEMBER = Member.of("polla@gmail.com", "polla99");

    @Test
    @DisplayName("전달 받은 데이터로 Reservation 객체를 정상적으로 생성한다.")
    void constructReservation() {
        Theme theme = THEME;
        Time time = TIME;
        Member member = MEMBER;
        Reservation reservation = Reservation.of(TOMORROW, time, theme, member, ReservationStatus.RESERVED);

        assertAll(
                () -> assertEquals(reservation.getTheme(), theme),
                () -> assertEquals(reservation.getReservationTime(), time),
                () -> assertEquals(reservation.getDate(), TOMORROW)
        );
    }

    @Test
    @DisplayName("과거의 날짜를 예약하려고 시도하는 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenReservationDateIsPast() {
        Throwable pastDateReservation = assertThrows(RoomEscapeException.class,
                () -> Reservation.of(BEFORE, TIME, THEME, MEMBER, ReservationStatus.RESERVED));

        assertEquals(ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION.getMessage(),
                pastDateReservation.getMessage());
    }
}
