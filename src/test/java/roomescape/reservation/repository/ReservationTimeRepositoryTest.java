package roomescape.reservation.repository;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.ReservationTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 등록된_시간_전부_찾기() {
        // when & then
        assertThat(reservationTimeRepository.findAll()).hasSize(0);
    }

    @Test
    void 시간_저장() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));

        // when
        reservationTimeRepository.save(reservationTime);

        // then
        assertThat(reservationTimeRepository.findAll()).hasSize(1);
    }

    @Test
    void 아이디_기준으로_시간_찾기() {
        // given
        final LocalTime startAt = LocalTime.of(20, 0);
        final ReservationTime reservationTime = new ReservationTime(startAt);
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        // when
        final ReservationTime foundReservationTime = reservationTimeRepository.findById(savedReservationTime.getId())
                .orElseThrow(IllegalArgumentException::new);

        // then
        assertThat(foundReservationTime.getStartAt()).isEqualTo(startAt);
    }

    @Test
    void 아이디_기준으로_시간_삭제() {
        // given
        final LocalTime startAt = LocalTime.of(20, 0);
        final ReservationTime reservationTime = new ReservationTime(startAt);
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        final Long id = savedReservationTime.getId();

        // when
        reservationTimeRepository.deleteById(id);

        // then
        assertThat(reservationTimeRepository.findById(id)).isEmpty();
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
        reservationTimeRepository.save(new ReservationTime(startAt));

        // when
        final boolean exists = reservationTimeRepository.existsByStartAt(LocalTime.of(hour, minute));

        // then
        assertThat(exists).isEqualTo(expected);
    }
}