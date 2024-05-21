package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;
import roomescape.service.dto.ReservationStatus;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @DisplayName("예약 시도 동시성 테스트")
    @Test
    void concurrency() throws InterruptedException {
        LocalDate date = LocalDate.parse("2024-07-01");
        ReservationSaveRequest requestA = new ReservationSaveRequest(1L, date, 1L, 1L);
        ReservationSaveRequest requestB = new ReservationSaveRequest(2L, date, 1L, 1L);
        ReservationSaveRequest requestC = new ReservationSaveRequest(3L, date, 1L, 1L);

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        AtomicReference<ReservationResponse> responseA = new AtomicReference<>();
        AtomicReference<ReservationResponse> responseB = new AtomicReference<>();
        AtomicReference<ReservationResponse> responseC = new AtomicReference<>();

        executorService.execute(() -> {
            try {
                responseA.set(reservationService.saveReservation(requestA));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        executorService.execute(() -> {
            try {
                responseB.set(reservationService.saveReservation(requestB));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(1000);
                responseC.set(reservationService.saveReservation(requestC));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await();


        assertAll(
                () -> assertThat(responseA.get().status()).isEqualTo(ReservationStatus.BOOKED),
                () -> assertThat(responseB.get().status()).isEqualTo(ReservationStatus.WAIT),
                () -> assertThat(responseC.get().status()).isEqualTo(ReservationStatus.WAIT)
        );
    }
}
