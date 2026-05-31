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
import roomescape.exception.DuplicateException;

@SpringBootTest
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingConcurrencyTest {

    // reservation-fixture.sql 기준:
    // 2026-06-05 / time_id=1 / theme_id=1 에 user_c의 예약이 존재
    // new-user는 해당 슬롯에 대기 없음 → N개 스레드가 동시에 대기 시도

    @Autowired
    private WaitingCommandService waitingCommandService;

    @Test
    @DisplayName("같은 슬롯에 동시 대기 생성 시 하나만 성공하고 나머지는 DuplicateException을 받는다.")
    void concurrentWaitingCreation_onlyOneSucceeds() throws InterruptedException {
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    waitingCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 1L);
                    successCount.incrementAndGet();
                } catch (DuplicateException e) {
                    duplicateCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(threadCount - 1);
    }
}
