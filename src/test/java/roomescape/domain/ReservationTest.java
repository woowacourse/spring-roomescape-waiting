package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.service.exception.DeletingException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    @DisplayName("자신의 예약인지 확인한다.")
    void validateOwn() {
        final Member member = new Member(1L, "레디", "redddy@gmail.com", "password", Role.ADMIN);
        final Member anotherMember = new Member(2L, "재즈", "gkatjraud@redddybabo.com", "password", Role.USER);
        final Reservation reservation = new Reservation(null, member, null, null, null);

        final long memberId = member.getId();
        final long anotherMemberId = anotherMember.getId();

        assertThatCode(() -> reservation.validateOwn(memberId))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> reservation.validateOwn(anotherMemberId))
                .isInstanceOf(DeletingException.class);

    }
}
