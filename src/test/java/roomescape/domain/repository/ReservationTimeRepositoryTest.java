package roomescape.domain.repository;

import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.ReservationTime;

@SpringBootTest
class ReservationTimeRepositoryTest {
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @AfterEach
    void tearDown() {
        reservationTimeRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 시간을 저장한다")
    void save_ShouldStorePersistence() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));

        // when
        reservationTimeRepository.save(reservationTime);

        // then
        Assertions.assertThat(reservationTimeRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("예약 시간의 영속성을 Id값으로 저장할 수 있다")
    void findById_ShouldGetPersistence() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));
        reservationTimeRepository.save(reservationTime);

        // when & then
        Assertions.assertThat(reservationTimeRepository.findById(reservationTime.getId()))
                .isPresent()
                .hasValue(reservationTime);
    }

    @Test
    @DisplayName("시작 시간으로 예약 시간에 대한 영속성의 존재 유무를 판단한다 - 참")
    void existsByStartAt_ShouldDetermineExistence() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));
        reservationTimeRepository.save(reservationTime);
        // when
        boolean result = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());
        // then
        Assertions.assertThat(result)
                .isTrue();
    }

    @Test
    @DisplayName("시작 시간으로 예약 시간에 대한 영속 존재 유무를 판단한다 - 거짓")
    void existsByStartAt_ShouldDetermineExistence_WhenDoesNotExists() {
        // when
        boolean result = reservationTimeRepository.existsByStartAt(LocalTime.of(1, 0));

        // then
        Assertions.assertThat(result)
                .isFalse();

    }

    @Test
    @DisplayName("예약 시간에 대한 영속성을 삭제할 수 있다")
    void delete_ShouldRemovePersistence() {
        ReservationTime reservationTime1 = new ReservationTime(LocalTime.of(1, 0));
        ReservationTime reservationTime2 = new ReservationTime(LocalTime.of(2, 0));
        ReservationTime reservationTime3 = new ReservationTime(LocalTime.of(3, 0));

        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        reservationTimeRepository.save(reservationTime3);

        // when
        reservationTimeRepository.delete(reservationTime1);

        // then
        Assertions.assertThat(reservationTimeRepository.findAll())
                .hasSize(2)
                .extracting("startAt")
                .containsExactlyInAnyOrder(LocalTime.of(2, 0), LocalTime.of(3, 0));
    }

    @Test
    @DisplayName("예약 시간의 모든 영속성을 삭제한다")
    void deleteAll_ShouldRemoveAllPersistence() {
        ReservationTime reservationTime1 = new ReservationTime(LocalTime.of(1, 0));
        ReservationTime reservationTime2 = new ReservationTime(LocalTime.of(2, 0));
        ReservationTime reservationTime3 = new ReservationTime(LocalTime.of(3, 0));

        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        reservationTimeRepository.save(reservationTime3);

        // when
        reservationTimeRepository.deleteAll();

        // then
        Assertions.assertThat(reservationTimeRepository.findAll())
                .isEmpty();
    }
}
