package roomescape.reservation.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.vo.Name;
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.role.MemberRole;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.theme.domain.Theme;
import roomescape.reservationtime.domain.ReservationTime;

class ReservationTest {

    private static final LocalTime TIME = LocalTime.of(9, 0);
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate BEFORE = LocalDate.now().minusDays(1);

    @Test
    @DisplayName("전달 받은 데이터로 Reservation 객체를 정상적으로 생성한다.")
    void constructReservation() {
        Theme theme = new Theme(1, new Name("미르"), "미르 방탈출", "썸네일 Url");
        ReservationTime time = new ReservationTime(1, TIME);
        Member member = new Member(
                1,
                new Name("polla"),
                "polla@gmail.com",
                "polla99",
                MemberRole.ADMIN);
        Reservation reservation = new Reservation(1L, TOMORROW, time, theme, member);

        assertAll(
                () -> assertEquals(theme, reservation.getTheme()),
                () -> assertEquals(time, reservation.getReservationTime()),
                () -> assertEquals(1, reservation.getId()),
                () -> assertEquals("polla", reservation.getMember().getName()),
                () -> assertEquals(TOMORROW, reservation.getDate())
        );
    }

    @Test
    @DisplayName("과거의 날짜를 예약하려고 시도하는 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenReservationDateIsPast() {
        ReservationTime time = new ReservationTime(1, TIME);
        Theme theme = new Theme(1, new Name("미르"), "미르 방탈출", "썸네일 Url");
        Member member = new Member(
                1,
                new Name("polla"),
                "polla@gmail.com",
                "polla99",
                MemberRole.ADMIN
        );

        Throwable pastDateReservation = assertThrows(RoomEscapeException.class,
                () -> new Reservation(BEFORE, time, theme, member));

        assertEquals(ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION.getMessage(),
                pastDateReservation.getMessage());
    }
}
