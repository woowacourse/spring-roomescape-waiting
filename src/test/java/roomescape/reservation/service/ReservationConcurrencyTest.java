package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.common.config.TestTimeConfig;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationCreateResponse;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.SlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest
@Import(TestTimeConfig.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationConcurrencyTest {

    private static final LocalDate FUTURE_DATE = LocalDate.parse("2026-08-05");
    private static final long AWAIT_SECONDS = 10L;
    private static final long LOCK_HOLDING_MILLIS = 300L;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        timeId = reservationTimeRepository.save(ReservationTime.create(LocalTime.parse("10:00"))).getId();
        themeId = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")).getId();
    }

    private ReservationRequest request(String name) {
        return new ReservationRequest(name, FUTURE_DATE, timeId, themeId);
    }

    @Test
    void 빈_슬롯에_동시에_예약하면_정확히_한_명만_점유하고_슬롯도_하나만_생성된다() throws Exception {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        // when : 슬롯 행이 없는 상태에서 10명이 동시에 신청한다 (슬롯 생성 경쟁 포함)
        List<Future<ReservationCreateResponse>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            String name = "회원" + i;
            futures.add(executor.submit(() -> {
                start.await();
                return reservationService.reserve(request(name));
            }));
        }
        start.countDown();

        List<String> statuses = new ArrayList<>();
        for (Future<ReservationCreateResponse> future : futures) {
            statuses.add(future.get(AWAIT_SECONDS, TimeUnit.SECONDS).status());
        }
        executor.shutdown();

        // then : 점유(결제 대기)는 정확히 1명, 나머지는 모두 대기, 슬롯 행은 1개
        assertThat(statuses).filteredOn("PENDING"::equals).hasSize(1);
        assertThat(countByStatus(ReservationStatus.PENDING)).isEqualTo(1);
        assertThat(countByStatus(ReservationStatus.WAITING)).isEqualTo(threadCount - 1);
        assertThat(countSlots()).isEqualTo(1);
    }

    @Test
    void 점유된_슬롯에_동시에_신청하면_모두_대기로_등록된다() throws Exception {
        // given
        reservationService.reserve(request("선점자"));

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        // when
        List<Future<ReservationCreateResponse>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            String name = "대기자" + i;
            futures.add(executor.submit(() -> {
                start.await();
                return reservationService.reserve(request(name));
            }));
        }
        start.countDown();

        List<String> statuses = new ArrayList<>();
        for (Future<ReservationCreateResponse> future : futures) {
            statuses.add(future.get(AWAIT_SECONDS, TimeUnit.SECONDS).status());
        }
        executor.shutdown();

        // then
        assertThat(statuses).allMatch("WAITING"::equals);
        assertThat(countByStatus(ReservationStatus.PENDING)).isEqualTo(1);
        assertThat(countByStatus(ReservationStatus.WAITING)).isEqualTo(threadCount);
    }

    @Test
    void 확정_취소와_신규_예약이_동시에_들어와도_확정은_정확히_한_명이다() throws Exception {
        // given : 브라운 확정, 어공 대기 1번
        ReservationCreateResponse confirmed = reservationService.reserve(request("브라운"));
        ReservationCreateResponse waiting = reservationService.reserve(request("어공"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        // when : 확정 취소와 신규 예약이 동시에 실행된다
        Future<?> cancelFuture = executor.submit(() -> {
            start.await();
            reservationService.cancel(confirmed.id());
            return null;
        });
        Future<ReservationCreateResponse> reserveFuture = executor.submit(() -> {
            start.await();
            return reservationService.reserve(request("신규"));
        });
        start.countDown();

        cancelFuture.get(AWAIT_SECONDS, TimeUnit.SECONDS);
        ReservationCreateResponse newcomer = reserveFuture.get(AWAIT_SECONDS, TimeUnit.SECONDS);
        executor.shutdown();

        // then : 어떤 순서로 실행되든 첫 대기자였던 어공이 확정, 신규는 대기
        assertThat(statusOf(waiting.id())).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(statusOf(newcomer.id())).isEqualTo(ReservationStatus.WAITING);
        assertThat(countByStatus(ReservationStatus.CONFIRMED)).isEqualTo(1);
    }

    @Test
    void 락_대기_중_승급된_예약을_취소해도_다음_대기자가_승급된다() throws Exception {
        // given : A 확정, B 대기 1번, C 대기 2번
        ReservationCreateResponse a = reservationService.reserve(request("A유저"));
        ReservationCreateResponse b = reservationService.reserve(request("B유저"));
        ReservationCreateResponse c = reservationService.reserve(request("C유저"));
        Long slotId = reservationRepository.findById(a.id()).orElseThrow().getSlotId();
        Reservation waitingB = reservationRepository.findById(b.id()).orElseThrow();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<Future<?>> cancelOfB = new AtomicReference<>();

        // when : B의 취소가 (대기 상태를 읽은 채) 슬롯 락 앞에 줄 서 있는 동안,
        //        락을 쥔 트랜잭션이 "A 취소 + B 승급"을 커밋한다
        transactionTemplate.executeWithoutResult(tx -> {
            slotRepository.lockForUpdate(slotId);
            cancelOfB.set(executor.submit(() -> {
                reservationService.cancel(b.id());
                return null;
            }));
            sleep(LOCK_HOLDING_MILLIS);
            reservationRepository.delete(a.id());
            reservationRepository.updateStatus(waitingB.promote());
        });
        cancelOfB.get().get(AWAIT_SECONDS, TimeUnit.SECONDS);
        executor.shutdown();

        // then : 락 해제 후 진행된 B의 취소는 "확정 취소"로 판정되어 C가 승급되어야 한다
        assertThat(statusOf(c.id())).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(countByStatus(ReservationStatus.CONFIRMED)).isEqualTo(1);
        assertThat(countByStatus(ReservationStatus.WAITING)).isZero();
    }

    @Test
    void 이미_처리된_취소를_락_대기_중인_요청이_중복_실행하면_NOT_FOUND로_실패한다() throws Exception {
        // given : A 확정, B 대기 1번, C 대기 2번
        ReservationCreateResponse a = reservationService.reserve(request("A유저"));
        ReservationCreateResponse b = reservationService.reserve(request("B유저"));
        ReservationCreateResponse c = reservationService.reserve(request("C유저"));
        Long slotId = reservationRepository.findById(a.id()).orElseThrow().getSlotId();
        Reservation waitingB = reservationRepository.findById(b.id()).orElseThrow();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<Future<?>> duplicateCancel = new AtomicReference<>();

        // when : 늦게 도착한 cancel(A)가 락 앞에 줄 서 있는 동안,
        //        먼저 도착한 취소(락 보유 트랜잭션)가 A 삭제 + B 승급을 커밋한다
        transactionTemplate.executeWithoutResult(tx -> {
            slotRepository.lockForUpdate(slotId);
            duplicateCancel.set(executor.submit(() -> {
                reservationService.cancel(a.id());
                return null;
            }));
            sleep(LOCK_HOLDING_MILLIS);
            reservationRepository.delete(a.id());
            reservationRepository.updateStatus(waitingB.promote());
        });
        Throwable thrown = catchThrowable(
                () -> duplicateCancel.get().get(AWAIT_SECONDS, TimeUnit.SECONDS));
        executor.shutdown();

        // then : 중복 취소는 NOT_FOUND로 실패하고, C가 과승급되지 않아야 한다
        assertThat(thrown).isInstanceOf(ExecutionException.class);
        assertThat(thrown.getCause()).isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode").isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
        assertThat(statusOf(b.id())).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(statusOf(c.id())).isEqualTo(ReservationStatus.WAITING);
        assertThat(countByStatus(ReservationStatus.CONFIRMED)).isEqualTo(1);
    }

    private ReservationStatus statusOf(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow().getStatus();
    }

    private long countByStatus(ReservationStatus status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE status = ?", Long.class, status.name());
        return count == null ? 0L : count;
    }

    private long countSlots() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM slot", Long.class);
        return count == null ? 0L : count;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
