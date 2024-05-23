package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.DEFAULT_ADMIN;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

class ReservationWaitingTest {

    @Test
    @DisplayName("예약이 없는 예약 대기는 생성할 수 없는지 확인")
    void createFailWhenNullReservation() {
        Assertions.assertThatThrownBy(() -> new ReservationWaiting(null, DEFAULT_ADMIN))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.WAITING_WITHOUT_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("예약 대기 회원이 없는 예약 대기는 생성할 수 없는지 확인")
    void createFailWhenNullWaitingMember() {
        Assertions.assertThatThrownBy(() -> new ReservationWaiting(DEFAULT_RESERVATION, null))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.WAITING_WITHOUT_MEMBER.getMessage());
    }

    @Test
    @DisplayName("해당 예약자가 예약 대기를 시도할 경우 생성할 수 없는지 확인")
    void createFailWhenAlreadyReservation() {
        Assertions.assertThatThrownBy(() -> new ReservationWaiting(DEFAULT_RESERVATION, DEFAULT_MEMBER))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.WAITING_AT_ALREADY_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("우선 순위를 잘 계산하는지 확인")
    void calculatePriority() {
        ReservationWaiting first = new ReservationWaiting();
        first.prePersist();
        ReservationWaiting second = new ReservationWaiting();
        second.prePersist();
        assertAll(
                () -> Assertions.assertThat(first.calculatePriority(List.of(first, second)))
                        .isEqualTo(1),
                () -> Assertions.assertThat(second.calculatePriority(List.of(first, second)))
                        .isEqualTo(2)
        );
    }
}
