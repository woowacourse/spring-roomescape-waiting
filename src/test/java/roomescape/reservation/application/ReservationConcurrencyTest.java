package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@SpringBootTest
public class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private Clock clock;

    private ReservationTime savedTime;
    private Theme savedTheme;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        targetDate = LocalDate.now(clock).plusDays(5);
        Theme theme = Theme.builder()
                .name("공포의 정신병원")
                .description("탈출해보세요")
                .thumbnailImageUrl("https://~~~~~")
                .durationTime(LocalTime.of(1, 0))
                .build();
        savedTheme = themeRepository.save(theme);

        ReservationTime time = ReservationTime.builder()
                .startAt(LocalTime.of(14, 0))
                .build();
        savedTime = timeRepository.save(time);
    }

    @Test
    @DisplayName("빈 슬롯에 10명이 동시에 확정 예약을 시도하면, 단 1명만 ACTIVE로 성공 해야 한다.")
    void selectActiveReservationConcurrencyTest() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String username = "크루" + i;
            executorService.submit(() -> {
                ReservationCreateCommand command = new ReservationCreateCommand(
                        username, targetDate, savedTime.getId(), savedTheme.getId()
                );
                try {
                    reservationService.addReservation(command);
                } catch (Exception e) {
                    System.out.println("[" + username + " 동시성 차단 완료]: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        List<Reservation> finalReservations = reservationRepository.findByThemeAndDate(savedTheme.getId(), targetDate);

        long activeCount = finalReservations.stream()
                .filter(r -> r.getStatus().equals(Status.ACTIVE))
                .count();

        assertThat(activeCount).isEqualTo(1);
    }
}
