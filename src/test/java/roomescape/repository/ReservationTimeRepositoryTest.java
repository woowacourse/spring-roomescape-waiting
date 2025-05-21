package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationTime;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("예약 시간을 저장하고 ID로 조회할 수 있다")
    void saveAndFindById() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));

        // when
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);
        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(savedTime.getId());

        // then
        assertThat(foundTime).isPresent();
        assertThat(foundTime.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("모든 예약 시간을 조회할 수 있다")
    void findAll() {
        // given
        ReservationTime time1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.createWithoutId(LocalTime.of(12, 0));
        ReservationTime time3 = ReservationTime.createWithoutId(LocalTime.of(14, 0));
        reservationTimeRepository.saveAll(List.of(time1, time2, time3));

        // when
        List<ReservationTime> times = reservationTimeRepository.findAll();

        // then
        assertThat(times).hasSize(3);
        assertThat(times).extracting("startAt")
                .containsExactlyInAnyOrder(
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        LocalTime.of(14, 0)
                );
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void delete() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);

        // when
        reservationTimeRepository.delete(savedTime);
        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(savedTime.getId());

        // then
        assertThat(foundTime).isEmpty();
    }

    @Test
    @DisplayName("예약 시간을 업데이트할 수 있다")
    void update() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);

        // when
        ReservationTime timeToUpdate = reservationTimeRepository.findById(savedTime.getId()).get();
        ReservationTime updatedTime = new ReservationTime(timeToUpdate.getId(), LocalTime.of(11, 30));
        reservationTimeRepository.save(updatedTime);

        // then
        ReservationTime foundTime = reservationTimeRepository.findById(savedTime.getId()).get();
        assertThat(foundTime.getStartAt()).isEqualTo(LocalTime.of(11, 30));
    }
}
