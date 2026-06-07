package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.Role;
import roomescape.dao.ReservationWaitDao;
import roomescape.domain.Member;
import roomescape.exception.reservation.ReservationAlreadyExistsException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/cleanup.sql")
class ReservationServiceConcurrencyTest {

    private static final int MEMBER_COUNT = 8;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationWaitDao reservationWaitDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO store (name) VALUES ('강남점')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, img_url) VALUES (?, ?, ?)",
                "테스트테마", "설명", "https://img.example.com/t.jpg");
        for (int i = 1; i <= MEMBER_COUNT; i++) {
            jdbcTemplate.update(
                    "INSERT INTO member (email, password, name, role, store_id) VALUES (?, ?, ?, 'USER', NULL)",
                    "user" + i + "@email.com", "password", "사용자" + i);
        }
    }

    @Test
    void 여러_사용자가_동시에_같은_슬롯을_예약해도_하나만_성공한다() throws InterruptedException {
        LocalDate date = LocalDate.of(2026, 12, 1);
        long timeId = 1L;
        long themeId = 1L;
        long storeId = 1L;

        int threadCount = MEMBER_COUNT;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        Queue<Class<?>> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            long memberId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.createReservation(memberId, date, timeId, themeId, storeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failures.add(e.getClass());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failures)
                .hasSize(threadCount - 1)
                .containsOnly(ReservationAlreadyExistsException.class);
    }

    @Test
    void 같은_예약을_동시에_취소해도_대기자는_한_명만_승급된다() throws InterruptedException {
        long ownerId = 1L;
        long firstWaiterId = 2L;
        long secondWaiterId = 3L;

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 10:00:00')",
                reservationId, firstWaiterId);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 11:00:00')",
                reservationId, secondWaiterId);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.deleteReservation(reservationId, ownerId);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        Long survived = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Long.class, reservationId);
        Long newOwner = jdbcTemplate.queryForObject(
                "SELECT member_id FROM reservation WHERE id = ?", Long.class, reservationId);
        Long remainingWaits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?", Long.class, reservationId);

        assertThat(survived).as("예약은 삭제되지 않고 양도되어야 한다").isEqualTo(1L);
        assertThat(newOwner).as("선두 대기자(2)에게만 양도되어야 한다").isEqualTo(firstWaiterId);
        assertThat(remainingWaits).as("취소 1번 → 승급 1명, 후순위 대기자(3)는 그대로 남아야 한다").isEqualTo(1L);
    }

    @Test
    void 대기가_없는_예약은_삭제된다() throws InterruptedException {
        long ownerId = 1L;
        long reservationId = 1L;

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.deleteReservation(reservationId, ownerId);
                } catch (Exception e) {
                    doneLatch.countDown();
                    return;
                }
                doneLatch.countDown();
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        Long survived = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Long.class, reservationId);
        assertThat(survived).as("예약은 삭제되어야 한다").isEqualTo(0L);
    }

    @Test
    void 승급과_대기취소가_교차해도_정합성이_깨지지_않는다() throws Exception {
        long ownerId = 1L;
        long firstWaiterId = 2L;
        long secondWaiterId = 3L;

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 10:00:00')",
                reservationId, firstWaiterId);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 11:00:00')",
                reservationId, secondWaiterId);

        CountDownLatch earliestRead = new CountDownLatch(1);
        CountDownLatch waitCancelled = new CountDownLatch(1);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Optional<Long> earliest = (Optional<Long>) invocation.callRealMethod();
            earliestRead.countDown();
            if (!waitCancelled.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("대기 취소(T2)가 제때 끝나지 않았습니다");
            }
            return earliest;
        }).when(reservationWaitDao).findEarliestMemberId(reservationId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> cancelTask = executor.submit(() -> {
            try {
                if (!earliestRead.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("승급(T1)이 선두를 읽지 못했습니다");
                }
                reservationService.deleteReservationWait(reservationId, firstWaiterId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } finally {
                waitCancelled.countDown();
            }
        });

        reservationService.deleteReservation(reservationId, ownerId);

        cancelTask.get(5, TimeUnit.SECONDS);
        executor.shutdown();

        Long survived = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Long.class, reservationId);
        Long owner = jdbcTemplate.queryForObject(
                "SELECT member_id FROM reservation WHERE id = ?", Long.class, reservationId);
        List<Long> remainingWaiters = jdbcTemplate.queryForList(
                "SELECT member_id FROM reservation_wait WHERE reservation_id = ? ORDER BY created_at, id",
                Long.class, reservationId);

        assertThat(survived).as("승급이므로 예약은 살아남아야 한다").isEqualTo(1L);
        assertThat(owner)
                .as("동시 대기취소가 끼어들어도 소유자는 선두 대기자(2)로 승급되어야 한다")
                .isEqualTo(firstWaiterId);
        assertThat(remainingWaiters)
                .as("승급된 2는 대기에서 빠지고, 후순위 3만 정확히 1명 남아야 한다(중복 승급·유실 없음)")
                .containsExactly(secondWaiterId);
        assertThat(remainingWaiters)
                .as("소유자가 동시에 대기자로 남아 있으면 안 된다")
                .doesNotContain(owner);
    }

    @Test
    void 승급_도중_본인_대기_재신청이_끼어들어도_소유자는_대기자가_되지_않는다() throws Exception {
        long ownerId = 1L;
        long firstWaiterId = 2L;

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 10:00:00')",
                reservationId, firstWaiterId);

        CountDownLatch rewaitReachedInsert = new CountDownLatch(1);
        CountDownLatch cancelPromotionCommitted = new CountDownLatch(1);

        doAnswer(invocation -> {
            rewaitReachedInsert.countDown();
            cancelPromotionCommitted.await(2, TimeUnit.SECONDS);
            return invocation.callRealMethod();
        }).when(reservationWaitDao).createReservationWait(firstWaiterId, reservationId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> rewaitTask = executor.submit(() -> {
            try {
                reservationService.createWait(firstWaiterId, reservationId);
            } catch (RuntimeException ignored) {
            }
        });

        if (!rewaitReachedInsert.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("대기 재신청이 삽입 직전까지 처리되지 못했습니다");
        }

        Future<?> cancelTask = executor.submit(() -> {
            try {
                reservationService.deleteReservation(reservationId, ownerId);
            } finally {
                cancelPromotionCommitted.countDown();
            }
        });

        cancelTask.get(10, TimeUnit.SECONDS);
        rewaitTask.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        Long owner = jdbcTemplate.queryForObject(
                "SELECT member_id FROM reservation WHERE id = ?", Long.class, reservationId);
        Long ownerWaitCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ? AND member_id = ?",
                Long.class, reservationId, owner);

        assertThat(owner).as("선두 대기자(2)가 승급되어 소유자가 되어야 한다").isEqualTo(firstWaiterId);
        assertThat(ownerWaitCount).as("승급된 소유자는 자기 예약의 대기자로 동시에 남을 수 없다").isEqualTo(0L);
    }

    @Test
    void 매니저_예약_강제삭제와_승급이_교차해도_orphan_대기나_예약이_남지_않는다() throws Exception {
        long ownerId = 1L;
        long firstWaiterId = 2L;
        Member manager = new Member(99L, "manager@email.com", "password", "매니저", Role.MANAGER, 1L);

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 10:00:00')",
                reservationId, firstWaiterId);

        CountDownLatch promotionParkedHoldingLock = new CountDownLatch(1);
        CountDownLatch resumePromotion = new CountDownLatch(1);

        doAnswer(invocation -> {
            Object earliest = invocation.callRealMethod();
            promotionParkedHoldingLock.countDown();
            resumePromotion.await(5, TimeUnit.SECONDS);
            return earliest;
        }).when(reservationWaitDao).findEarliestMemberId(reservationId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> promotionTask = executor.submit(
                () -> reservationService.deleteReservation(reservationId, ownerId));

        if (!promotionParkedHoldingLock.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("승급이 예약 락을 가진 채 멈추지 못했습니다");
        }

        Future<?> managerDeleteTask = executor.submit(() -> {
            try {
                reservationService.deleteByManager(reservationId, manager);
            } catch (RuntimeException ignored) {
            }
        });

        resumePromotion.countDown();

        promotionTask.get(10, TimeUnit.SECONDS);
        managerDeleteTask.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        Long survived = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Long.class, reservationId);
        Long remainingWaits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?", Long.class, reservationId);

        assertThat(survived).as("매니저가 강제 삭제한 예약은 승급으로 부활하지 않아야 한다").isEqualTo(0L);
        assertThat(remainingWaits).as("예약이 사라지면 매달린 대기도 남지 않아야 한다(orphan 없음)").isEqualTo(0L);
    }

    @Test
    void 두_예약을_같은_슬롯으로_동시에_옮기면_하나만_성공한다() throws InterruptedException {
        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (1, '2027-01-01', 1, 1, 1)");
        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (2, '2027-01-02', 1, 1, 1)");
        Long firstId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE date = '2027-01-01'", Long.class);
        Long secondId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE date = '2027-01-02'", Long.class);

        LocalDate target = LocalDate.of(2027, 3, 3);
        long timeId = 1L;
        long[] ids = {firstId, secondId};
        long[] owners = {1L, 2L};

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger();
        Queue<Class<?>> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < 2; i++) {
            long id = ids[i];
            long memberId = owners[i];
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.updateReservation(id, target, memberId, timeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failures.add(e.getClass());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        Long atTarget = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE store_id = 1 AND date = '2027-03-03' AND time_id = 1 AND theme_id = 1",
                Long.class);

        assertThat(successCount.get()).as("같은 슬롯으로의 동시 이동은 하나만 성공해야 한다").isEqualTo(1);
        assertThat(failures).as("나머지는 unique 충돌로 막혀야 한다")
                .containsExactly(ReservationAlreadyExistsException.class);
        assertThat(atTarget).as("목표 슬롯에는 정확히 한 건만 존재해야 한다").isEqualTo(1L);
    }
}
