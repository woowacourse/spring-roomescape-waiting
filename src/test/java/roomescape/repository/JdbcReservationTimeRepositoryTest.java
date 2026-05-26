package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.ReservationTime;

@JdbcTest
@Import({
        JdbcReservationTimeRepository.class,
})
class JdbcReservationTimeRepositoryTest {

    @Autowired
    JdbcReservationTimeRepository jdbcReservationTimeRepository;

    @DisplayName("예약시간을 저장한다")
    @Test
    void 예약시간을_저장하면_id를_부여한다() {
        // given
        ReservationTime reservationTime = ReservationTime.create(LocalTime.parse("10:00"));

        // when
        ReservationTime saved = jdbcReservationTimeRepository.save(reservationTime);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @DisplayName("예약시간을 id로 조회한다")
    @Test
    void 예약시간을_id로_조회한다() {
        // given
        ReservationTime reservationTime = ReservationTime.create(LocalTime.parse("10:00"));
        ReservationTime saved = jdbcReservationTimeRepository.save(reservationTime);

        // when
        Optional<ReservationTime> result = jdbcReservationTimeRepository.findById(saved.getId());

        // then
        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @DisplayName("저장된 모든 예약시간을 조회한다")
    @Test
    void 저장된_모든_예약시간을_조회한다() {
        // given
        ReservationTime reservationTime1 = jdbcReservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime reservationTime2 = jdbcReservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));

        // when
        List<ReservationTime> found = jdbcReservationTimeRepository.findAll();

        // then
        assertThat(found)
                .hasSize(2)
                .containsExactlyInAnyOrder(reservationTime1, reservationTime2);
    }

    @DisplayName("id에 해당하는 예약시간을 삭제한다")
    @Test
    void 예약시간을_삭제한다() {
        // given
        ReservationTime reservationTime = jdbcReservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));

        // when
        jdbcReservationTimeRepository.delete(reservationTime.getId());
        Optional<ReservationTime> result = jdbcReservationTimeRepository.findById(
                reservationTime.getId()
        );

        // then
        assertThat(result).isNotPresent();
    }
}
