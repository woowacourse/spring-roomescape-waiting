package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class WaitingTest {

    private static final Reservation DEFAULT_RESERVATION = new Reservation(
            1L, new Member(1L, "브라운", "brown@abc.com"),
            LocalDate.of(2024, 8, 15),
            new ReservationTime(1L, LocalTime.of(19, 0)),
            new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final Member DEFAULT_MEMBER = new Member(2L, "브리", "bri@abc.com");

    @DisplayName("예약이 비어있을 때 예외를 던진다.")
    @Test
    void validateTest_whenReservationIsNull() {
        assertThatThrownBy(() -> new Waiting(null, DEFAULT_MEMBER))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("멤버가 비어있을 떄 예외를 던진다.")
    @Test
    void validateTest_whenMemberIsNull() {
        assertThatThrownBy(() -> new Waiting(DEFAULT_RESERVATION, null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("예약 대기 정보를 통해, 예약을 확정할 수 있다.")
    @Test
    void confirmReservationTest() {
        Waiting waiting = new Waiting(DEFAULT_RESERVATION, DEFAULT_MEMBER);

        waiting.confirmReservation();

        Member reservationOwner = waiting.getReservation().getMember();
        assertThat(reservationOwner).isEqualTo(DEFAULT_MEMBER);
    }

    @DisplayName("예약 대기 주인을 확인할 수 있다.")
    @Test
    void isNotWaitingOwnerTest_whenSameMemberId() {
        Waiting waiting = new Waiting(DEFAULT_RESERVATION, DEFAULT_MEMBER);

        boolean actual = waiting.isNotWaitingOwner(DEFAULT_MEMBER.getId());

        assertThat(actual).isFalse();
    }

    @DisplayName("예약 대기 주인을 확인할 수 있다.")
    @Test
    void isNotWaitingOwnerTest_whenDifferentMemberId() {
        Waiting waiting = new Waiting(DEFAULT_RESERVATION, DEFAULT_MEMBER);

        boolean actual = waiting.isNotWaitingOwner(3L);

        assertThat(actual).isTrue();
    }
}
