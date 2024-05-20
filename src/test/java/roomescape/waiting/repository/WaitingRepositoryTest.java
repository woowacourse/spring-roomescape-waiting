package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.test.RepositoryTest;

class WaitingRepositoryTest extends RepositoryTest {
    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("예약 아이디, 멤버 아이디에 해당하는 예약 대기가 존재하는지 확인한다. - 존재할 때")
    @Test
    void existsByReservationIdAndMemberIdTest_whenExist() {
        Long reservationId = 5L;
        Long memberId = 4L;

        boolean actual = waitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);

        assertThat(actual).isTrue();
    }

    @DisplayName("예약 아이디, 멤버 아이디에 해당하는 예약 대기가 존재하는지 확인한다. - 존재하지 않을 때")
    @Test
    void existsByReservationAndMemberTest_whenNotExist() {
        Long reservationId = 5L;
        Long memberId = 3L;

        boolean actual = waitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);

        assertThat(actual).isFalse();
    }
}
