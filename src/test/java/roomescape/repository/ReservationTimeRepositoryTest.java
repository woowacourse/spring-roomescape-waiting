package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(ReservationTimeRepository.class)
class ReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation_time");
    }

    @Test
    void 존재하는_id로_조회하면_예약_시간을_Optional로_반환한다() {
        ReservationTime saved = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(11, 30)));

        Optional<ReservationTime> result = reservationTimeRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getStartAt()).isEqualTo(LocalTime.of(11, 30));
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        Optional<ReservationTime> result = reservationTimeRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void 여러_시간을_저장한_뒤_전체_조회하면_모든_시간을_반환한다() {
        ReservationTime saved1 = reservationTimeRepository.save(new ReservationTime(
                null, LocalTime.of(10, 0)));
        ReservationTime saved2 = reservationTimeRepository.save(new ReservationTime(
                null, LocalTime.of(11, 0)));

        List<ReservationTime> result = reservationTimeRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartAt()).isEqualTo(saved1.getStartAt());
        assertThat(result.get(1).getStartAt()).isEqualTo(saved2.getStartAt());
    }

    @Test
    void 시간을_저장하면_생성된_id와_시작_시간을_반환한다() {
        ReservationTime saved = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(10, 0)));

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 같은_시작_시간을_중복_생성할_수_없다() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        assertThatThrownBy(() -> reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void 존재하는_id로_삭제하면_해당_시간이_삭제된다() {
        ReservationTime saved = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(10, 0)));

        reservationTimeRepository.deleteById(saved.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM reservation_time WHERE id = ?", Integer.class, saved.getId());
        assertThat(count).isZero();
    }
}
