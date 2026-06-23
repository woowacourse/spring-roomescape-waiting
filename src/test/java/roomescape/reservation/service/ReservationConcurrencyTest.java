package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.dto.ReservationRequest;

@SpringBootTest
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
    }

    @Test
    @DisplayName("동시에 2개의 예약 생성 요청이 들어왔을 때 DB에는 1개만 저장된다")
    void 동시_예약_생성_요청_시_1개만_저장된다() throws InterruptedException {
        ReservationRequest request = new ReservationRequest("user1", LocalDate.of(2099, 12, 1), 1L, 1L);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> caughtExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.createReservation(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    caughtExceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        caughtExceptions.forEach(e ->
                System.out.println("발생한 예외: " + e.getClass().getName() + " / " + e.getMessage())
        );

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(caughtExceptions).hasSize(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("동시에 2개의 예약 삭제 요청이 들어왔을 때 오류 없이 합 번만 처리된다.")
    void 동시_예약_삭제_요청_시_1개만_삭제된다() throws InterruptedException {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        Long futureReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> caughtExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.deleteReservation(futureReservationId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    caughtExceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        caughtExceptions.forEach(e ->
                System.out.println("발생한 예외: " + e.getClass().getName() + " / " + e.getMessage())
        );

        assertThat(successCount.get()).isEqualTo(2);
        assertThat(caughtExceptions).hasSize(0);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("대기자가 있는 예약을 동시에 2개의 스레드가 삭제할 때 대기자는 정확히 1번만 승격된다")
    void 대기자_있는_예약_동시_삭제_시_대기자는_1번만_승격된다() throws InterruptedException {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, date, time_id, theme_id) VALUES ('user2', '2099-12-01', 1, 1)");

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Exception> caughtExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.deleteReservation(reservationId);
                } catch (Exception e) {
                    caughtExceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        caughtExceptions.forEach(e ->
                System.out.println("발생한 예외: " + e.getClass().getName() + " / " + e.getMessage())
        );

        Integer reservationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        assertThat(reservationCount).isEqualTo(1);

        Integer waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_waiting", Integer.class);
        assertThat(waitingCount).isEqualTo(0);
    }
}