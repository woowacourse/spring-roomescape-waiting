package roomescape.unit.repository.reservationtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.reservationtime.JpaReservationTimeRepository;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Test
    void 주어진_시간을_가지는_예약시간이_있는지_확인할_수_있다() {
        // Given
        LocalTime startAt = LocalTime.now();
        jpaReservationTimeRepository.save(new ReservationTime(null, startAt));

        // When & Then
        assertThat(jpaReservationTimeRepository.existsByTime(startAt)).isTrue();
    }

    @Test
    void 잘못된_시간으로는_저장된_예약시간이_존재하지_않는다는_응답을_받아야_한다() {
        // Given
        LocalTime startAt = LocalTime.now();
        jpaReservationTimeRepository.save(new ReservationTime(null, startAt));

        // When & Then
        assertThat(jpaReservationTimeRepository.existsByTime(LocalTime.now().plusMinutes(5))).isFalse();
    }
}
