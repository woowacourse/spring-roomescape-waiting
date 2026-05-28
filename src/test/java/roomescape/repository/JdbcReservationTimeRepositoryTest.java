package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
    ReservationTimeRepository reservationTimeRepository;

    @Test
    void 예약시간을_저장하면_id를_부여한다() {
        // given
        ReservationTime reservationTime = ReservationTime.create(LocalTime.parse("10:00"));

        // when
        ReservationTime saved = reservationTimeRepository.save(reservationTime);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 예약시간을_id로_조회한다() {
        // given
        ReservationTime reservationTime = ReservationTime.create(LocalTime.parse("10:00"));
        ReservationTime saved = reservationTimeRepository.save(reservationTime);

        // when
        Optional<ReservationTime> result = reservationTimeRepository.findById(saved.getId());

        // then
        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    void 저장된_모든_예약시간을_조회한다() {
        // given
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));

        // when
        List<ReservationTime> found = reservationTimeRepository.findAll();

        // then
        assertThat(found)
                .hasSize(2)
                .containsExactlyInAnyOrder(reservationTime1, reservationTime2);
    }

    @Test
    void 예약시간을_삭제한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));

        // when
        reservationTimeRepository.delete(reservationTime.getId());
        Optional<ReservationTime> result = reservationTimeRepository.findById(
                reservationTime.getId()
        );

        // then
        assertThat(result).isNotPresent();
    }
}
