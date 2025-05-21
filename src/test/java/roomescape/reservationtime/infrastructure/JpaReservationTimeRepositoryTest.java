package roomescape.reservationtime.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;

@DataJpaTest
@Import(TestConfig.class)
class JpaReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void existsByStartAt() {
        ReservationTime reservationTime = TestFixture.makeReservationTime();
        reservationTimeRepository.save(reservationTime);

        boolean existsByStartAt = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());
        assertThat(existsByStartAt).isTrue();
    }
}
