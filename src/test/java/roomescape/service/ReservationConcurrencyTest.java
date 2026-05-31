package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.FixedClockConfig;

@SpringBootTest
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationConcurrencyTest {

    // reservation-fixture.sql 기준:
    // id=2: user_b / 2026-06-05 / time_id=2 / theme_id=1 (미래)
    // id=3: user_c / 2026-06-05 / time_id=1 / theme_id=1 (미래)
    // 두 예약을 동시에 같은 슬롯(2026-07-01 / time_id=3)으로 변경 → 하나만 가능

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Test
    @DisplayName("서로 다른 예약을 같은 슬롯으로 동시에 변경하면 하나만 성공한다.")
    void concurrentReservationUpdate_onlyOneSucceeds() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        new Thread(() -> {
            try {
                startLatch.await();
                reservationCommandService.update(2L, "user_b", LocalDate.of(2026, 7, 1), 3L);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                startLatch.await();
                reservationCommandService.update(3L, "user_c", LocalDate.of(2026, 7, 1), 3L);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        assertThat(successCount.get() + failCount.get()).isEqualTo(2);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
    }
}
