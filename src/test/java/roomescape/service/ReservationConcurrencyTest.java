package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.service.dto.ReservationCreateCommand;

@SpringBootTest
class ReservationConcurrencyTest {

    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);
    private static final int CONCURRENCY = 20;

    @Autowired
    private AdminReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("같은 빈 슬롯에 동시에 예약하면 확정은 정확히 1건이어야 한다")
    void 동시에_같은_슬롯을_예약해도_확정은_하나여야_한다() throws InterruptedException {
        long timeId = insertTime();
        long themeId = insertTheme();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch ready = new CountDownLatch(CONCURRENCY);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CONCURRENCY);
        AtomicInteger failures = new AtomicInteger();

        for (int i = 0; i < CONCURRENCY; i++) {
            String name = "user" + i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reservationService.create(new ReservationCreateCommand(name, DATE, timeId, themeId));
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        int confirmed = countConfirmed(timeId, themeId);
        assertThat(confirmed)
                .as("같은 슬롯의 활성 확정 예약은 정원(1)을 넘으면 안 된다")
                .isEqualTo(1);
    }

    private int countConfirmed(long timeId, long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation "
                        + "WHERE date = ? AND time_id = ? AND theme_id = ? AND status = 'CONFIRMED'",
                Integer.class, Date.valueOf(DATE), timeId, themeId);
        return count == null ? 0 : count;
    }

    private long insertTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

    private long insertTheme() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES ('동시성테마', '설명', 'http://x')");
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
    }
}
