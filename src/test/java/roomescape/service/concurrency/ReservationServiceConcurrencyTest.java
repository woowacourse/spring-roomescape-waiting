package roomescape.service.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exception.ConflictException;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.service.ReservationService;
import roomescape.service.dto.command.ReservationCommand;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationServiceConcurrencyTest {
    private final String name = "브라운";
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);

    private final Long timeId = 1L;
    private final ReservationTime time = new ReservationTime(timeId, LocalTime.parse("10:00"));

    private final Long themeId = 1L;
    private final Theme theme = new Theme(themeId, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/thumbnail"));

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("여러 사용자가 동시에 같은 슬롯으로 예약 변경을 시도하면, 1명만 성공하고 나머지는 예외가 발생한다.")
    void changeDateTime_Concurrency() throws InterruptedException {
        Long reservationId = 1L;
        LocalDate newDate = futureDate.plusDays(1);

        ReservationCommand command = new ReservationCommand(name, newDate, time.getId(), theme.getId());

        int threadCount = 10;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();

                        reservationService.changeDateTime(reservationId, command);
                        successCount.incrementAndGet();
                    } catch (ConflictException e) {
                        conflictCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        failCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();
        }

        assertThat(successCount.get() + conflictCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);

        Reservation finalReservation = reservationDao.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getEventSlot().date()).isEqualTo(newDate);
    }
}
