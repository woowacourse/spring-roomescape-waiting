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
import roomescape.service.dto.ReservationUpdateCommand;

@SpringBootTest
class ReservationConcurrencyTest {

    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);
    private static final int CONCURRENCY = 20;

    @Autowired
    private AdminReservationService reservationService;

    @Autowired
    private UserReservationService userReservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("같은 빈 슬롯에 동시에 예약하면 확정은 정확히 1건이어야 한다")
    void 동시에_같은_슬롯을_예약해도_확정은_하나여야_한다() throws InterruptedException {
        long timeId = insertTime();
        long themeId = insertTheme("동시성테마-생성");

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

    @Test
    @DisplayName("같은 확정 예약을 동시에 취소해도 승급은 한 번만 일어나 확정은 1건을 유지한다")
    void 동시에_같은_확정예약을_취소해도_확정은_하나여야_한다() throws InterruptedException {
        long timeId = insertTime();
        long themeId = insertTheme("동시성테마-취소");
        long confirmedId = reservationService.create(
                new ReservationCreateCommand("owner", DATE, timeId, themeId)).id();
        for (int i = 0; i < 5; i++) {
            reservationService.create(new ReservationCreateCommand("waiter" + i, DATE, timeId, themeId));
        }

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reservationService.cancel(confirmedId);
                } catch (Exception ignored) {
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
                .as("확정 취소 시 첫 대기자 1명만 승급되어야 한다(이중 승급 금지)")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("빈 슬롯으로의 예약 변경과 신규 예약이 동시에 일어나도 확정은 1건이어야 한다")
    void 변경_진입과_신규_예약이_경합해도_확정은_하나여야_한다() throws InterruptedException {
        long fromTimeId = insertTime("10:00");
        long toTimeId = insertTime("11:00");
        long themeId = insertTheme("동시성테마-변경경합");
        long moverId = reservationService.create(
                new ReservationCreateCommand("mover", DATE, fromTimeId, themeId)).id();

        int creators = 9;
        int total = creators + 1;
        ExecutorService pool = Executors.newFixedThreadPool(total);
        CountDownLatch ready = new CountDownLatch(total);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(total);

        pool.submit(() -> {
            ready.countDown();
            try {
                start.await();
                userReservationService.update(new ReservationUpdateCommand(moverId, "mover", DATE, toTimeId));
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });
        for (int i = 0; i < creators; i++) {
            String name = "newuser" + i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reservationService.create(new ReservationCreateCommand(name, DATE, toTimeId, themeId));
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        int confirmed = countConfirmed(toTimeId, themeId);
        assertThat(confirmed)
                .as("변경 진입과 신규 예약이 경합해도 슬롯 확정은 1건이어야 한다")
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
        return insertTime("10:00");
    }

    private long insertTime(String startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

    private long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, '설명', 'http://x')", name);
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
    }
}
