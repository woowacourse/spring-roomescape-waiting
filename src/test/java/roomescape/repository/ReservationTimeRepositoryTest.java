package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.TestFixture;

class ReservationTimeRepositoryTest extends DBTest {

    @DisplayName("해당 시간이 존재하는지 확인한다.")
    @Test
    void existByStartAt() {
        // given
        timeRepository.save(TestFixture.getReservationTime10AM());

        // when
        boolean exist = timeRepository.existsByStartAt(TestFixture.getReservationTime10AM().getStartAt());

        // then
        assertThat(exist).isTrue();
    }
}
