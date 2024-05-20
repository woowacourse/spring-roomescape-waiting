package roomescape.domain.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.ReservationTimeFixture.TEN_RESERVATION_TIME;

import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;
import roomescape.domain.reservation.repository.reservationTime.JpaReservationTimeRepository;
import roomescape.domain.reservation.repository.reservationTime.ReservationTimeRepository;
import roomescape.domain.reservation.repository.reservationTime.ReservationTimeRepositoryImpl;

class ReservationTimeRepositoryImplTest extends RepositoryTest {


    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new ReservationTimeRepositoryImpl(jpaReservationTimeRepository);
        jpaReservationTimeRepository.save(TEN_RESERVATION_TIME);
    }

    @AfterEach
    void setDown() {
        jpaReservationTimeRepository.deleteAll();
    }

    @DisplayName("동일한 시간의 예약시간이 존재하는지 확인할 수 있습니다.")
    @Test
    void should_return_true_when_same_time_is_already_exist() {
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
    }

    @DisplayName("동일한 시간의 예약시간이 존재하지 않는 경우를 확인할 수 있습니다")
    @Test
    void should_return_false_when_same_time_is_no_exist() {
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }
}
