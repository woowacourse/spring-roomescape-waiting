package roomescape.dao;

import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.ReservationTime;
import roomescape.exception.time.NotFoundTimeException;

@SpringBootTest
class ReservationTimeDaoTest {
    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    @DisplayName("예약 시간을 저장할 수 있다")
    void save_ShouldStorePersistence() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));

        // when
        ReservationTime savedReservationTime = reservationTimeDao.save(reservationTime);

        // then
        Assertions.assertThat(reservationTimeDao.findById(savedReservationTime.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void delete_ShouldRemovePersistence() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));
        ReservationTime savedReservationTime = reservationTimeDao.save(reservationTime);

        // when
        reservationTimeDao.delete(savedReservationTime);

        // then
        Assertions.assertThat(reservationTimeDao.findById(savedReservationTime.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("없는 예약 시간에 대한 삭제를 시도하면 예외를 발생시킨다")
    void delete_ShouldThrowException_WhenReservationTimeDoesNotExist() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTimeDao.delete(reservationTime))
                .isInstanceOf(NotFoundTimeException.class);
    }

}
