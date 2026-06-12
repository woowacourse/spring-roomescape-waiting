package roomescape.reservationtime.repository.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("/clear.sql")
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("예약 시간을 저장하고 조회한다")
    void saveAndFindReservationTime() {
        ReservationTime reservationTime = ReservationTime.create(LocalTime.of(10, 0));

        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(savedTime.getId());
        assertThat(foundTime).isPresent();
        assertThat(foundTime.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시간 목록을 id 순서로 조회한다")
    void findReservationTimesOrderById() {
        ReservationTime firstTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTime secondTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)));

        List<ReservationTime> times = reservationTimeRepository.findAll();

        assertThat(times)
                .extracting(ReservationTime::getId)
                .containsExactly(firstTime.getId(), secondTime.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 조회하면 빈 Optional을 반환한다")
    void returnEmptyOptionalWhenReservationTimeDoesNotExist() {
        Optional<ReservationTime> foundTime = reservationTimeRepository.findById(1L);

        assertThat(foundTime).isEmpty();
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void deleteReservationTime() {
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        boolean deleted = reservationTimeRepository.delete(savedTime.getId());

        assertThat(deleted).isTrue();
        assertThat(reservationTimeRepository.findById(savedTime.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 false를 반환한다")
    void returnFalseWhenDeletingNonExistingReservationTime() {
        boolean deleted = reservationTimeRepository.delete(1L);

        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("해당 시간에 예약이 있으면 예약 시간을 삭제할 수 없다")
    void cannotDeleteReservationTimeInUse() {
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "링",
                "공포 테마",
                "http:~"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                "2026-08-05",
                savedTime.getId(),
                1L
        );
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, customer_email, slot_id) VALUES (?, ?, ?)",
                "브라운",
                "brown@example.com",
                1L
        );

        assertThatThrownBy(() -> reservationTimeRepository.delete(savedTime.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
