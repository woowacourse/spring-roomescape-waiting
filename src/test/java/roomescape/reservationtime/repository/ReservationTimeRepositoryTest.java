package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;

@DataJpaTest
@Import(TestConfig.class)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void existsByStartAt() {
        ReservationTime reservationTime = TestFixture.makeReservationTime(1L);
        reservationTimeRepository.save(reservationTime);

        boolean existsByStartAt = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());
        assertThat(existsByStartAt).isTrue();
    }
}
