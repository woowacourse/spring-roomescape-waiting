package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.RoomescapeApplication;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.WaitingRequestDTO;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@SpringBootTest(classes = RoomescapeApplication.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class WaitingConcurrencyTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 동시에_10개의_대기등록이_요청되면_10개의_대기가_중복_없이_등록된다() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        LocalDate date = LocalDate.of(2026, 6, 1);

        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com")
        );
        ReservationSlot slot = ReservationSlot.of(date, time, theme);

        reservationRepository.save(
                Reservation.create("코코", slot)
        );

        for (int i = 0; i < threadCount; i++) {
            String guestName = "사용자" + i;

            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    waitingService.addWaiting(new WaitingRequestDTO(
                            guestName,
                            date,
                            time.getId(),
                            theme.getId()
                    ));
                } catch (Exception e) {
                    System.out.println("등록 실패: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        List<Long> numbers = waitingRepository.findAll().stream()
                .filter(w -> w.getReservationSlot().equals(slot))
                .map(Waiting::getWaitingNumber)
                .toList();

        Assertions.assertThat(numbers).hasSize(10);
        Assertions.assertThat(numbers).doesNotHaveDuplicates();
        Assertions.assertThat(numbers)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }
}
