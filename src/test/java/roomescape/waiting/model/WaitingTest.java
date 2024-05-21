package roomescape.waiting.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;

class WaitingTest {

    @Test
    @DisplayName("해당하는 회원, 예약과 동일한 예약 대기가 존재한다면, 참을 반환한다.")
    void isNotSameMember() {
        // given
        Reservation reservation = ReservationFixture.getOneWithId(1L);
        Member member = MemberFixture.getOneWithId(1L);

        Waiting waiting = new Waiting(reservation, member);

        assertTrue(waiting.isNotSameMember(member.getId() + 1));
    }

    @Test
    @DisplayName("해당하는 회원, 예약과 동일한 예약 대기가 존재하지 않는다면, 거짓을 반환한다.")
    void isNotSameMember_WhenNotSame() {
        // given
        Reservation reservation = ReservationFixture.getOneWithId(1L);
        Member member = MemberFixture.getOneWithId(1L);

        Waiting waiting = new Waiting(reservation, member);

        assertFalse(waiting.isNotSameMember(member.getId()));
    }
}
