package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("특정 아이디의 예약 시간을 조회한다.")
    @Test
    void findByIdTest() {
        // When
        final Long reservationTimeId = 1L;
        final Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(reservationTimeId);

        // Then
        assertThat(reservationTime.isPresent()).isTrue();
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void findAllTest() {
        // When
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        // Then
        assertThat(reservationTimes).hasSize(8);
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void saveTest() {
        // Given
        final LocalTime startAt = LocalTime.of(1, 0);
        final ReservationTime reservationTime = new ReservationTime(startAt);

        // When
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        // Then
        final List<ReservationTime> savedReservationTimes = reservationTimeRepository.findAll();
        assertAll(
                () -> assertThat(savedReservationTimes).hasSize(9),
                () -> assertThat(savedReservationTime.getId()).isEqualTo(9L),
                () -> assertThat(savedReservationTime.getStartAt()).isEqualTo(startAt)
        );
    }

    @DisplayName("테마 정보를 삭제한다.")
    @Test
    void deleteByIdTest() {
        // When
        reservationTimeRepository.deleteById(2L);

        // Then
        final long count = reservationTimeRepository.count();
        assertThat(count).isEqualTo(7);
    }

    @DisplayName("특정 시간값의 예약 시간이 존재하는지 조회한다.")
    @Test
    void existByStartAtTest() {
        // When
        final LocalTime startAt = LocalTime.of(13, 30);
        final boolean isExist = reservationTimeRepository.existsByStartAt(startAt);

        // Then
        assertThat(isExist).isTrue();
    }
}
