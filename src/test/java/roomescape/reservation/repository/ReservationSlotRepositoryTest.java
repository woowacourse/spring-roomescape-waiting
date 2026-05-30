package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.reservation.domain.ReservationSlot;

@JdbcTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReservationSlotRepositoryTest {

    private static final Long DATE_ID = 1L;
    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;

    private JdbcReservationSlotRepository reservationSlotRepository;
    private TransactionTemplate transactionTemplate;
    private ExecutorService executorService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        reservationSlotRepository = new JdbcReservationSlotRepository(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Nested
    @DisplayName("lockByDateTimeAndThemeId 메서드는")
    class LockByDateTimeAndThemeIdTest {

        @Test
        @DisplayName("같은 슬롯의 락이 해제될 때까지 다른 트랜잭션을 대기시킨다")
        void 성공() throws Exception {
            // given
            reservationSlotRepository.saveIfAbsent(
                ReservationSlot.create(DATE_ID, TIME_ID, THEME_ID));
            CountDownLatch firstLocked = new CountDownLatch(1);
            CountDownLatch releaseFirstLock = new CountDownLatch(1);
            CountDownLatch secondLocked = new CountDownLatch(1);

            // when
            Future<?> first = executorService.submit(() -> lockSlotUntil(firstLocked,
                releaseFirstLock));
            assertThat(firstLocked.await(1, TimeUnit.SECONDS)).isTrue();

            Future<?> second = executorService.submit(() -> transactionTemplate.executeWithoutResult(
                status -> {
                    reservationSlotRepository.lockByDateTimeAndThemeId(DATE_ID, TIME_ID, THEME_ID);
                    secondLocked.countDown();
                }));

            // then
            assertThat(secondLocked.await(300, TimeUnit.MILLISECONDS)).isFalse();

            releaseFirstLock.countDown();
            assertThat(secondLocked.await(1, TimeUnit.SECONDS)).isTrue();
            first.get(1, TimeUnit.SECONDS);
            second.get(1, TimeUnit.SECONDS);
        }
    }

    private void lockSlotUntil(CountDownLatch firstLocked, CountDownLatch releaseFirstLock) {
        transactionTemplate.executeWithoutResult(status -> {
            reservationSlotRepository.lockByDateTimeAndThemeId(DATE_ID, TIME_ID, THEME_ID);
            firstLocked.countDown();
            await(releaseFirstLock);
        });
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
