package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.dto.PageResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;
import roomescape.test_config.fixture.SQLFixtureGenerator;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestClockConfig.class, SQLFixtureGenerator.class, ReservationConcurrencyTest.ConcurrencyTestConfig.class})
@Sql(value = "/acceptance-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SQLFixtureGenerator sqlFixtureGenerator;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void resetConcurrencyTestDouble() {
        if (reservationRepository instanceof SynchronizedReservationRepository synchronizedReservationRepository) {
            synchronizedReservationRepository.reset();
        }
    }

    @Test
    @DisplayName("동시에 같은 날짜, 시간, 테마로 예약하면 확정 예약은 하나만 생성되어야 한다.")
    void create_concurrently_sameSlot_onlyOneConfirmed() throws Exception {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);

        // when
        executeConcurrently(
                () -> reservationService.create("브라운", date, time.getId(), theme.getId()),
                () -> reservationService.create("포비", date, time.getId(), theme.getId())
        );

        // then
        long confirmedCount = countConfirmedReservations(date, time.getId(), theme.getId());
        assertThat(confirmedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("서로 다른 두 사용자가 동시에 같은 슬롯으로 예약을 수정하면 확정 예약은 하나만 유지되어야 한다.")
    void editDateTime_concurrently_sameSlot_onlyOneConfirmed() throws Exception {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime targetTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        ReservationTime brownTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(11, 0));
        ReservationTime pobiTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate originalDate = LocalDate.of(2025, 5, 11);
        LocalDate targetDate = LocalDate.of(2025, 5, 12);

        Reservation brown = sqlFixtureGenerator.insertReservation(
                "브라운", originalDate, brownTime, theme, Status.CONFIRMED);
        Reservation pobi = sqlFixtureGenerator.insertReservation(
                "포비", originalDate, pobiTime, theme, Status.CONFIRMED);

        // when
        executeConcurrently(
                () -> reservationService.editDateTime(brown.getId(), targetDate, targetTime.getId(), brown.getGuestName()),
                () -> reservationService.editDateTime(pobi.getId(), targetDate, targetTime.getId(), pobi.getGuestName())
        );

        // then
        assertThat(countReservationsByStatus(targetDate, targetTime.getId(), theme.getId(), Status.CONFIRMED)).isEqualTo(1);
        assertThat(countReservationsByStatus(targetDate, targetTime.getId(), theme.getId(), Status.WAITING)).isEqualTo(1);
    }

    private void executeConcurrently(Runnable first, Runnable second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<?> firstFuture = executor.submit(() -> {
                await(startLatch);
                first.run();
            });
            Future<?> secondFuture = executor.submit(() -> {
                await(startLatch);
                second.run();
            });

            startLatch.countDown();
            firstFuture.get(3, TimeUnit.SECONDS);
            secondFuture.get(3, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private long countConfirmedReservations(LocalDate date, Long timeId, Long themeId) {
        return countReservationsByStatus(date, timeId, themeId, Status.CONFIRMED);
    }

    private long countReservationsByStatus(LocalDate date, Long timeId, Long themeId, Status status) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE date = :date
                          AND time_id = :timeId
                          AND theme_id = :themeId
                          AND status = :status
                        """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", timeId)
                        .addValue("themeId", themeId)
                        .addValue("status", status.toString()),
                Long.class);

        return count == null ? 0 : count;
    }

    @TestConfiguration
    static class ConcurrencyTestConfig {

        @Bean
        @Primary
        ReservationRepository synchronizedReservationRepository(JdbcReservationRepository delegate) {
            return new SynchronizedReservationRepository(delegate);
        }
    }

    private static class SynchronizedReservationRepository implements ReservationRepository {

        private final ReservationRepository delegate;
        private final CyclicBarrier concurrentCreateBarrier = new CyclicBarrier(2);
        private final AtomicInteger confirmedStatusCheckCount = new AtomicInteger();

        private SynchronizedReservationRepository(ReservationRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<Reservation> findById(Long id) {
            return delegate.findById(id);
        }

        @Override
        public Optional<ReservationWaitingDto> findWaitingById(Long id) {
            return delegate.findWaitingById(id);
        }

        @Override
        public PageResult<Reservation> findAllByStatusCanceledNot(int page, int size) {
            return delegate.findAllByStatusCanceledNot(page, size);
        }

        @Override
        public List<ReservationWaitingDto> findWaitingAllByGuestName(String guestName) {
            return delegate.findWaitingAllByGuestName(guestName);
        }

        @Override
        public Optional<Reservation> findBySlotAndStatusWaitingAndWaitingNumberIsOne(
                LocalDate date, Long timeId, Long themeId) {
            return delegate.findBySlotAndStatusWaitingAndWaitingNumberIsOne(date, timeId, themeId);
        }

        @Override
        public Reservation save(Reservation reservation) {
            return delegate.save(reservation);
        }

        @Override
        public boolean updateDateAndTimeAndStatus(
                Long id, LocalDate date, Long timeId, Status status, LocalDateTime lastModifiedAt) {
            return delegate.updateDateAndTimeAndStatus(id, date, timeId, status, lastModifiedAt);
        }

        @Override
        public boolean updateStatus(Long id, Status status) {
            return delegate.updateStatus(id, status);
        }

        @Override
        public boolean cancelById(Long id) {
            return delegate.cancelById(id);
        }

        @Override
        public boolean existsBySlotAndGuestNameExceptCanceled(
                LocalDate date, Long timeId, Long themeId, String guestName) {
            return delegate.existsBySlotAndGuestNameExceptCanceled(date, timeId, themeId, guestName);
        }

        @Override
        public boolean existsBySlotAndStatusConfirmed(LocalDate date, Long timeId, Long themeId) {
            boolean exists = delegate.existsBySlotAndStatusConfirmed(date, timeId, themeId);
            if (confirmedStatusCheckCount.incrementAndGet() <= 2) {
                awaitConcurrentCreate();
            }
            return exists;
        }

        @Override
        public boolean existsBySlotExceptReservation(LocalDate date, Long timeId, Long themeId, Long excludedId) {
            return delegate.existsBySlotExceptReservation(date, timeId, themeId, excludedId);
        }

        @Override
        public boolean existByTimeId(Long timeId) {
            return delegate.existByTimeId(timeId);
        }

        @Override
        public boolean existByThemeId(Long themeId) {
            return delegate.existByThemeId(themeId);
        }

        private void awaitConcurrentCreate() {
            try {
                concurrentCreateBarrier.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } catch (BrokenBarrierException | TimeoutException e) {
                throw new IllegalStateException(e);
            }
        }

        private void reset() {
            confirmedStatusCheckCount.set(0);
            concurrentCreateBarrier.reset();
        }
    }
}
