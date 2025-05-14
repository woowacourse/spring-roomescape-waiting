package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import roomescape.reservation.domain.ReservationTime;
import roomescape.utils.JdbcTemplateUtils;

@ActiveProfiles("test")
@JdbcTest
@Import({ReservationTimeDao.class})
class ReservationTimeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReservationTimeDao reservationTimeRepository;

    @AfterEach
    void tearDown() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
    }

    @DisplayName("id에 따라 예약 시간을 반환한다.")
    @Test
    void test1() {
        // given
        Long id = 1L;
        LocalTime now = LocalTime.now();
        saveReservationTime(id, now);

        // when
        ReservationTime result = reservationTimeRepository.findById(id).get();

        // then
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStartAt()).isEqualTo(now);
    }

    private void saveReservationTime(Long id, LocalTime startAt) {
        String sql = "insert into reservation_time (id, start_at) values (?, ?)";
        jdbcTemplate.update(sql, id, startAt);
    }

    @DisplayName("예약 시간을 삭제한다.")
    @Nested
    class delete {

        @DisplayName("해당 ID를 삭제한다.")
        @Test
        void test1() {
            // given
            Long id = 1L;
            LocalTime now = LocalTime.of(9, 0);
            String sql = "insert into reservation_time(id, start_at) values(?, ?)";
            jdbcTemplate.update(sql, id, now);

            // when
            assertThatCode(() -> reservationTimeRepository.deleteById(id))
                    .doesNotThrowAnyException();
        }
    }

    @DisplayName("예약 시간을 저장한다.")
    @Nested
    class save {

        @DisplayName("성공 테스트")
        @Test
        void test1() {
            // given
            LocalTime now = LocalTime.now();
            ReservationTime time = ReservationTime.withoutId(now);

            // when
            ReservationTime saved = reservationTimeRepository.save(time);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStartAt()).isEqualTo(now);
        }
    }
}
