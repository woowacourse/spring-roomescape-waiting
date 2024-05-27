package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.ReservationService;
import roomescape.application.dto.request.reservation.ReservationRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

@SpringBootTest
class ConcurrencyTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약 동시성 테스트")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/times.sql", "/test-data/themes.sql"})
    void 예약_동시성_테스트() throws InterruptedException {
        // given
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        ReservationRequest request = new ReservationRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L,
                1L);

        // when
        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.saveReservation(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(Reservation::isReserved)
                .toList();

        assertThat(reservations)
                .hasSize(1);
        assertThat(successCount.get())
                .isEqualTo(1);
        assertThat(failCount.get())
                .isEqualTo(count - 1);
    }
}
