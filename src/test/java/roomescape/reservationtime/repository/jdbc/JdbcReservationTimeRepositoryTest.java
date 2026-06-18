package roomescape.reservationtime.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@ActiveProfiles("jdbc")
@Sql("/clear.sql")
class JdbcReservationTimeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    JdbcReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Test
    void 예약_시간을_저장하고_조회한다() {
        ReservationTime reservationTime = ReservationTime.create(LocalTime.of(10, 0));

        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(savedTime.getId());
        assertThat(foundTime).isPresent();
        assertThat(foundTime.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 예약_시간_목록을_id_순서로_조회한다() {
        ReservationTime firstTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTime secondTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));

        List<ReservationTime> times = reservationTimeRepository.findAll();

        assertThat(times)
                .extracting(ReservationTime::getId)
                .containsExactly(firstTime.getId(), secondTime.getId());
    }

    @Test
    void 존재하지_않는_예약_시간을_조회하면_빈_Optional을_반환한다() {
        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(1L);

        assertThat(foundTime).isEmpty();
    }

    @Test
    void 예약_시간을_삭제한다() {
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        boolean deleted = reservationTimeRepository.delete(savedTime.getId());

        assertThat(deleted).isTrue();
        assertThat(reservationTimeRepository.findById(savedTime.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_예약_시간을_삭제하면_false를_반환한다() {
        boolean deleted = reservationTimeRepository.delete(1L);

        assertThat(deleted).isFalse();
    }

    @Test
    void 해당_시간에_예약이_있으면_예약_시간을_삭제할_수_없다() {
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "링",
                "공포 테마",
                "http:~"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운",
                "2026-08-05",
                savedTime.getId(),
                1L
        );

        assertThatThrownBy(() -> reservationTimeRepository.delete(savedTime.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
