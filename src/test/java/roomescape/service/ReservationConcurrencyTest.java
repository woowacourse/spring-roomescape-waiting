package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationTimeCreateCommand;
import roomescape.service.dto.ThemeCreateCommand;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationConcurrencyTest {

    private static final String 매트 = "매트";
    private static final String 루드비코 = "루드비코";
    private static final LocalDate 예약_날짜 = LocalDate.of(2099, 12, 31);

    @Autowired
    private AdminReservationService adminReservationService;
    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private ReservationRepository reservationRepository;

    private Long time1Id;
    private Long time2Id;
    private Long time3Id;
    private Long themeId;

    @BeforeEach
    void setUp() {
        time1Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(10, 0))).id();
        time2Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(11, 0))).id();
        time3Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(12, 0))).id();

        themeId = themeService.create(new ThemeCreateCommand("테스트 테마", "설명", "url")).id();
    }

    @DisplayName("동시에 동일한 예약을 생성하려고 할 때 데이터베이스 Unique 제약 조건에 의해 단 하나만 성공해야 한다")
    @Test
    void 동일한_예약_동시_생성시_하나만_성공한다() throws InterruptedException {
        ReservationCreateCommand command = new ReservationCreateCommand(
                매트,
                예약_날짜,
                time3Id,
                themeId
        );

        ConcurrencyResult result = 동시_실행(10, () -> adminReservationService.create(command));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(9);
        assertThat(특정_시간의_예약_개수_조회(time3Id)).isEqualTo(1);
    }

    private ConcurrencyResult 동시_실행(int threadCount, Runnable action) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    action.run();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        return new ConcurrencyResult(successCount.get(), failCount.get());
    }

    private long 특정_시간의_예약_개수_조회(Long timeId) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.time().getId().equals(timeId))
                .count();
    }

    private record ConcurrencyResult(int successCount, int failCount) {
    }
}
