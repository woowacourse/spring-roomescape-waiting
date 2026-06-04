package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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

    private Long mattReservationId;
    private Long rudevicoReservationId;

    @BeforeEach
    void setUp() {
        time1Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(10, 0))).id();
        time2Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(11, 0))).id();
        time3Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(12, 0))).id();

        themeId = themeService.create(new ThemeCreateCommand("테스트 테마", "설명", "url")).id();

        mattReservationId = adminReservationService.reserveOnSlot(
                new ReservationCreateCommand(매트, 예약_날짜, time1Id, themeId)).id();
        rudevicoReservationId = adminReservationService.reserveOnSlot(
                new ReservationCreateCommand(루드비코, 예약_날짜, time2Id, themeId)).id();
    }

    @Disabled("동시성 관리는 아직")
    @DisplayName("동시에 동일한 사용자가 예약을 생성할 때 중복 생성이 차단된다")
    @Test
    void 동일한_예약_동시_생성시_하나만_성공한다() throws InterruptedException {
        ReservationCreateCommand command = new ReservationCreateCommand(매트, 예약_날짜, time3Id, themeId);

        ConcurrencyResult result = 동시_실행(10, () -> adminReservationService.reserveOnSlot(command));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(9);
        assertThat(특정_시간의_예약_개수_조회(time3Id)).isEqualTo(1);
    }

    @Disabled("")
    @DisplayName("동시에 서로 다른 사용자가 같은 시간에 예약하면 모두 성공하고 대기 순번이 부여된다")
    @Test
    void 서로_다른_사용자가_동시_예약시_모두_성공하고_대기순번이_부여된다() throws InterruptedException {
        int threadCount = 10;
        Runnable[] actions = new Runnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            String userName = "사용자" + i;
            ReservationCreateCommand command = new ReservationCreateCommand(userName, 예약_날짜, time3Id, themeId);
            actions[i] = () -> adminReservationService.reserveOnSlot(command);
        }

        ConcurrencyResult result = 동시_실행(actions);

        assertThat(result.successCount()).isEqualTo(threadCount);
        assertThat(result.failCount()).isZero();
        assertThat(특정_시간의_예약_개수_조회(time3Id)).isEqualTo(threadCount);
    }

    @Disabled("H2 DB 환경에서만 터져서 일단 비활성화")
    @DisplayName("예약 생성과 해당 시간 삭제가 동시에 일어날 때 외래 키 제약 조건 등에 의해 정합성이 보장된다")
    @Test
    void 예약_생성과_해당_시간_삭제가_동시에_일어날_때_정합성이_보장된다() throws InterruptedException {
        ReservationCreateCommand command = new ReservationCreateCommand("새로운사용자", 예약_날짜, time3Id, themeId);

        ConcurrencyResult result = 동시_실행(
                () -> adminReservationService.reserveOnSlot(command),
                () -> reservationTimeService.delete(time3Id)
        );

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(1);
    }

    private ConcurrencyResult 동시_실행(int threadCount, Runnable action) throws InterruptedException {
        Runnable[] actions = new Runnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            actions[i] = action;
        }
        return 동시_실행(actions);
    }

    private ConcurrencyResult 동시_실행(Runnable... actions) throws InterruptedException {
        int threadCount = actions.length;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (Runnable action : actions) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    action.run();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();
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
