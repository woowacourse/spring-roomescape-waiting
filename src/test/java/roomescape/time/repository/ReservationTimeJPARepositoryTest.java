package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.time.domain.ReservableTime;
import roomescape.time.domain.ReservationTime;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-reservable-time-data.sql"})
public class ReservationTimeJPARepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("모든 예약 시간을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        // when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        // then
        assertThat(reservationTimes).hasSize(3);
    }

    @DisplayName("해당 시간이 존재하는 지 확인할 수 있다.")
    @Test
    void testExistByStartAt() {
        // given
        // when
        // then
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(17, 0))).isFalse();
    }

    @DisplayName("예약 가능한 시간을 조회할 수 있다.")
    @Test
    void testReservableTime() {
        // given

        // when
        List<ReservableTime> allReservableTime = reservationTimeRepository.findAllReservableTime(
                LocalDate.of(2025, 5, 1), 1);
        // then
        assertThat(allReservableTime).hasSize(3);
        assertThat(allReservableTime.getFirst().isBooked()).isTrue();
        assertThat(allReservableTime.get(1).isBooked()).isTrue();
        assertThat(allReservableTime.get(2).isBooked()).isFalse();
    }
}
