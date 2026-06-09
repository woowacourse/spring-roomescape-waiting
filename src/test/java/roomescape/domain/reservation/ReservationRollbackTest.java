package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.domain.waiting.WaitingRepository;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRollbackTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void 예약_취소_중_대기_삭제가_실패하면_예약_삭제와_대기_승격을_롤백한다() {
        Long themeId = insertTheme("롤백테마");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("예약자", LocalDate.of(2099, 12, 31), timeId, themeId);
        insertWaiting("대기자1", LocalDate.of(2099, 12, 31), timeId, themeId);
        ((FailingWaitingRepository) waitingRepository).failOnDelete();

        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId, "예약자"))
            .isInstanceOf(IllegalStateException.class);

        assertThat(reservationNames()).containsExactly("예약자");
        assertThat(waitingNames()).containsExactly("대기자1");
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES (?, '설명', 'https://example.com/image.jpg')",
            name
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private Long insertTime(String startAt, String finishAt) {
        jdbcTemplate.update(
            "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
            startAt, finishAt
        );
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private Long insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
            "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            name, date, timeId, themeId
        );
        return jdbcTemplate.queryForObject("SELECT id FROM reservation WHERE name = ?", Long.class, name);
    }

    private void insertWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
            "INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            name, date, timeId, themeId
        );
    }

    private List<String> reservationNames() {
        return jdbcTemplate.queryForList("SELECT name FROM reservation ORDER BY id", String.class);
    }

    private List<String> waitingNames() {
        return jdbcTemplate.queryForList("SELECT name FROM waiting ORDER BY id", String.class);
    }

    @TestConfiguration
    static class FailingWaitingRepositoryConfig {

        @Bean
        @Primary
        WaitingRepository failingWaitingRepository(JdbcTemplate jdbcTemplate) {
            return new FailingWaitingRepository(jdbcTemplate);
        }
    }

    static class FailingWaitingRepository extends WaitingRepository {

        private final AtomicBoolean failOnDelete = new AtomicBoolean(false);

        FailingWaitingRepository(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        void failOnDelete() {
            failOnDelete.set(true);
        }

        @Override
        public void deleteById(Long id) {
            if (failOnDelete.get()) {
                throw new IllegalStateException("대기 삭제 실패");
            }
            super.deleteById(id);
        }
    }
}
