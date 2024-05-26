package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;

class ReservationTimeRepositoryTest extends DBTest {

    @DisplayName("해당 시간이 존재하는지 확인한다.")
    @Test
    void existByStartAt() {
        // given
        timeRepository.save(RESERVATION_TIME_10AM);

        // when
        boolean exist = timeRepository.existsByStartAt(RESERVATION_TIME_10AM.getStartAt());

        // then
        assertThat(exist).isTrue();
    }
}
