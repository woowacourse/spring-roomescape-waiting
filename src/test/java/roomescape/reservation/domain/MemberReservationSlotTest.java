package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationFixture.getNextMonthReservation;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@DisplayName("사용자 예약 내역 도메인")
class MemberReservationSlotTest {
    @DisplayName("동일한 id는 같은 사용자 예약이다.")
    @Test
    void equals() {
        //given
        Long id = 1L;
        Member member = new Member(2L, "notUse", "초코칩", "dev.chocochip@gmail.com", Role.USER);
        ReservationTime noon = getNoon();
        Theme theme = getTheme1();
        ReservationSlot nextDayReservationSlot = getNextDayReservation(noon, theme);
        ReservationSlot nextMonthReservationSlot = getNextMonthReservation(noon, theme);

        //when
        MemberReservation memberReservation1 = new MemberReservation(id, member, nextDayReservationSlot);
        MemberReservation memberReservation2 = new MemberReservation(id, member, nextMonthReservationSlot);

        //then
        assertThat(memberReservation1).isEqualTo(memberReservation2);
    }
}
