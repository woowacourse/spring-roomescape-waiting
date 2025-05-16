package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.time.domain.ReservationTime;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 특정_시작_시간을_가진_예약_시간이_존재하는지_확인한다() {
        // given
        LocalTime startAt = LocalTime.of(14, 0); // 14:00
        ReservationTime reservationTime = ReservationTime.open(startAt);
        reservationTimeRepository.save(reservationTime);

        // when
        boolean exists = reservationTimeRepository.existsByStartAt(startAt);
        boolean notExists = reservationTimeRepository.existsByStartAt(LocalTime.of(15, 0));

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void 예약_시간을_저장하고_ID로_조회한다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTime reservationTime = ReservationTime.open(startAt);

        // when
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);
        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(savedTime.getId());

        // then
        assertThat(foundTime).isPresent();
        assertThat(foundTime.get().getStartAt()).isEqualTo(startAt);
    }

    @Test
    void 모든_예약_시간을_조회한다() {
        // given
        ReservationTime time1 = ReservationTime.open(LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.open(LocalTime.of(11, 0));
        ReservationTime time3 = ReservationTime.open(LocalTime.of(12, 0));
        reservationTimeRepository.saveAll(List.of(time1, time2, time3));

        // when
        List<ReservationTime> times = reservationTimeRepository.findAll();

        // then
        assertThat(times).hasSize(3);
        assertThat(times).extracting("startAt")
                .containsExactlyInAnyOrder(
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 0)
                );
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        ReservationTime reservationTime = ReservationTime.open(LocalTime.of(10, 0));
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        // when
        reservationTimeRepository.deleteById(savedTime.getId());

        // then
        assertThat(reservationTimeRepository.findById(savedTime.getId())).isEmpty();
    }
} 