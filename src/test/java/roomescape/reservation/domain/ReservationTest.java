package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.ReservationSlotFixture.getNextDayReservationSlot;
import static roomescape.fixture.ReservationSlotFixture.getNextMonthReservationSlot;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@DisplayName("사용자 예약 내역 도메인")
class ReservationTest {

    @DisplayName("동일한 id는 같은 사용자 예약이다.")
    @Test
    void equals() {
        //given
        Long id = 1L;
        Member member = new Member(2L, "notUse", "초코칩", "dev.chocochip@gmail.com", Role.USER);
        ReservationTime noon = getNoon();
        Theme theme = getTheme1();
        ReservationSlot nextDayReservationSlot = getNextDayReservationSlot(noon, theme);
        ReservationSlot nextMonthReservationSlot = getNextMonthReservationSlot(noon, theme);

        //when
        Reservation reservation1 = new Reservation(id, member, nextDayReservationSlot);
        Reservation reservation2 = new Reservation(id, member, nextMonthReservationSlot);

        //then
        assertThat(reservation1).isEqualTo(reservation2);
    }
}
