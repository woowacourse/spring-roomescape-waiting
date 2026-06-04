package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.DuplicateEntityException;
import roomescape.service.command.ReservationCommand;
import roomescape.support.BaseIntegrationTest;
import roomescape.support.DatabaseCleaner;
import roomescape.support.ReservationDataSource;
import roomescape.support.TestDateTimes;

class ReservationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationDataSource reservationDataSource;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clear();
        reservationDataSource.insertTheme("공포의 테마", "공포 테마", "https://image.com/image.png");
        reservationDataSource.insertReservationTime(TestDateTimes.defaultTime());
    }

    @Test
    void 동시에_2명이_예약하면_1명만_성공해야_한다() throws InterruptedException {
        // given
        ReservationCommand command = new ReservationCommand("이프", TestDateTimes.tomorrow(), 1L, 1L);
        int threadCount = 2;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        // when
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        reservationService.reserve(command);
                    } catch (DuplicateEntityException e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        latch.await();

        // then: DB에 예약이 딱 하나만 있어야 하고, DataIntergrityViolation 예외가 한 번 발생해야 됨.
        assertThat(errorCount.get()).isEqualTo(1);
        assertThat(reservationDataSource.countReservations()).isEqualTo(1);
    }
}
