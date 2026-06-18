package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.service.ReservationService;
import roomescape.application.service.command.ReservationCommand;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.exception.DuplicateEntityException;

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
        assertThat(reservationDataSource.countReservations()).isEqualTo(1);
    }

    @Test
    void 예약_취소_시_대기자를_예약으로_승격한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(2);
        ReservationSlotResult reservation = reservationService.reserve(new ReservationCommand("이프", date, 1L, 1L));
        reservationService.addWaiting(new ReservationCommand("라텔", date, 1L, 1L));

        // when
        reservationService.cancelReservation(reservation.reservation().id());

        // then
        List<String> statuses = reservationDataSource.findReservationStatusesBySlotId(reservation.slotId());
        assertThat(statuses)
                .containsExactlyInAnyOrder(
                        "이프:RESERVED:CANCELED",
                        "라텔:RESERVED:ACTIVE"
                );
    }
}
