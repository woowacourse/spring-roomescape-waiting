package roomescape.reservationwaiting.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;

@SpringBootTest
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationWaitingConcurrencyTest {

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
    }

    @Test
    @DisplayName("동시에 2개의 예약 대기 생성 요청이 들어왔을 때 DB에는 1개만 저장된다")
    void 동시_예약_대기_생성_요청_시_1개만_저장된다() throws InterruptedException {
        ReservationWaitingRequest request = new ReservationWaitingRequest("user2", 1L);
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
                    reservationWaitingService.createWaiting(request);
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

        Assertions.assertThat(successCount.get()).isEqualTo(1);
        Assertions.assertThat(caughtExceptions).hasSize(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_waiting", Integer.class);
        Assertions.assertThat(count).isEqualTo(1);
    }
}
