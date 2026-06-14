package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.reservationtime.JdbcReservationTimeRepository;

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    private JdbcReservationTimeRepository jdbcReservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 시간 전체 조회")
    void reservationTime_findAll_test() {
        //given & when
        LocalTime time = LocalTime.parse("11:00");

        ReservationTime nonIdReservationTime = ReservationTime.createNew(time);
        jdbcReservationTimeRepository.save(nonIdReservationTime);

        Optional<ReservationTime> reservationTime = jdbcReservationTimeRepository.findAll()
                .stream()
                .findFirst();
        //then
        assertThat(reservationTime).isNotEmpty();
    }

    @Test
    @DisplayName("예약 시간 저장")
    void reservationTime_save_test() {
        //given
        LocalTime time = LocalTime.parse("11:00");
        ReservationTime nonIdReservationTime = ReservationTime.createNew(time);

        //when
        ReservationTime result = jdbcReservationTimeRepository.save(nonIdReservationTime);
        ReservationTime saved = jdbcReservationTimeRepository.findById(result.getId())
                .orElseThrow();
        //then
        assertThat(result).isEqualTo(saved);
    }

    @Test
    @DisplayName("예약 시간 저장 예외")
    void save_duplicate_test() {
        //given
        LocalTime time = LocalTime.parse("11:00");

        //when
        jdbcReservationTimeRepository.save(ReservationTime.createNew(time));

        //then
        assertThrows(PersistenceConflictException.class, () -> {
            jdbcReservationTimeRepository.save(ReservationTime.createNew(time));
        });
    }

    @Test
    @DisplayName("예약 시간 삭제")
    void reservationTime_delete_test() {
        // given
        LocalTime time = LocalTime.parse("11:00");
        ReservationTime nonIdReservationTime = ReservationTime.createNew(time);
        ReservationTime reservationTime = jdbcReservationTimeRepository.save(nonIdReservationTime);
        int beforeSize = jdbcReservationTimeRepository.findAll().size();

        // when
        int affectedRowCount = jdbcReservationTimeRepository.deleteById(reservationTime.getId());

        // then
        int afterSize = jdbcReservationTimeRepository.findAll().size();

        assertThat(affectedRowCount).isOne();
        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 ID는 삭제 건수가 0이다")
    void reservationTime_delete_not_found_test() {
        // when
        int affectedRowCount = jdbcReservationTimeRepository.deleteById(999L);

        // then
        assertThat(affectedRowCount).isZero();
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
