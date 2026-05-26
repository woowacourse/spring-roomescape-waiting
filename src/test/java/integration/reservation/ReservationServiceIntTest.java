package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.DuplicateEntityException;
import roomescape.service.ReservationService;
import roomescape.service.command.ReservationCommand;

class ReservationServiceIntTest extends BaseIntegrationTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationDataSource reservationDataSource;

    @BeforeEach
    void setUp() {
        reservationDataSource.clearTable();
        reservationDataSource.clearId();
        reservationDataSource.insertTheme("공포의 테마", "공포 테마", "https://image.com/image.png");
        reservationDataSource.insertReservationTime(LocalTime.of(10, 0));
    }

    @Test
    void 동시에_2명이_예약하면_1명만_성공해야_한다() throws InterruptedException {
        // given
        ReservationCommand command = new ReservationCommand("이프", LocalDate.now().plusDays(1), 1L, 1L);
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
        assertThat(reservationService.getAllReservations()).hasSize(1);
    }
}
