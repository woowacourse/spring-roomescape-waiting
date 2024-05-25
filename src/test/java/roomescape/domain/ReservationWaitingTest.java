package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationWaitingTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new ReservationWaiting(VALID_MEMBER, VALID_RESERVATION, 1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("대기 번호가 0 이하일 경우 예외가 발생한다.")
    @Test
    void createFail_NegativePriority() {
        assertThatThrownBy(() -> new ReservationWaiting(VALID_MEMBER, VALID_RESERVATION, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약자와 예약 대기자와 Member가 동일하면 예외가 발생한다.")
    @Test
    void createDomain_sameMember() {
        Member member = new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER);
        Reservation reservation = new Reservation(1L, member, VALID_RESERVATION_DATE, VALID_RESERVATION_TIME,
                VALID_THEME);
        assertThatThrownBy(() -> ReservationWaiting.create(member, reservation, 1L))
                .isInstanceOf(IllegalStateException.class);
    }
}
