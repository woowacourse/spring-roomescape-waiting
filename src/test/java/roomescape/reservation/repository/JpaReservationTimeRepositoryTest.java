package roomescape.reservation.repository;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.time.JpaReservationTimeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Test
    void 등록된_시간_전부_찾기() {
        // when & then
        assertThat(jpaReservationTimeRepository.findAll()).hasSize(0);
    }

    @Test
    void 시간_저장() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));

        // when
        jpaReservationTimeRepository.save(reservationTime);

        // then
        assertThat(jpaReservationTimeRepository.findAll()).hasSize(1);
    }

    @Test
    void 아이디_기준으로_시간_찾기() {
        // given
        final LocalTime startAt = LocalTime.of(20, 0);
        final ReservationTime reservationTime = new ReservationTime(startAt);
        final ReservationTime savedReservationTime = jpaReservationTimeRepository.save(reservationTime);

        // when
        final ReservationTime foundReservationTime = jpaReservationTimeRepository.findById(savedReservationTime.getId())
                .orElseThrow(IllegalArgumentException::new);

        // then
        assertThat(foundReservationTime.getStartAt()).isEqualTo(startAt);
    }

    @Test
    void 아이디_기준으로_시간_삭제() {
        // given
        final LocalTime startAt = LocalTime.of(20, 0);
        final ReservationTime reservationTime = new ReservationTime(startAt);
        final ReservationTime savedReservationTime = jpaReservationTimeRepository.save(reservationTime);
        final Long id = savedReservationTime.getId();

        // when
        jpaReservationTimeRepository.deleteById(id);

        // then
        assertThat(jpaReservationTimeRepository.findById(id)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value =
            {
                    "20,0,true", "21,0,false"
            }
    )
    void 시간_존재하는지_확인(final int hour, final int minute, final boolean expected) {
        // given
        final LocalTime startAt = LocalTime.of(20, 0);
        jpaReservationTimeRepository.save(new ReservationTime(startAt));

        // when
        final boolean exists = jpaReservationTimeRepository.existsByStartAt(LocalTime.of(hour, minute));

        // then
        assertThat(exists).isEqualTo(expected);
    }
}