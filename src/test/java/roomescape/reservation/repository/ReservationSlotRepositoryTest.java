package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ReservationSlotRepositoryTest {

    private static final Long DATE_ID = 1L;
    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    @Autowired
    private ReservationDateRepository reservationDateRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    private TransactionTemplate transactionTemplate;
    private ExecutorService executorService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
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
            ReservationDate reservationDate = reservationDateRepository.save(
                ReservationDate.create(LocalDate.now().plusDays(1)));
            ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(Theme.create("theme", "description", "thumbnail-url"));
            reservationSlotRepository.saveAndFlush(
                ReservationSlot.create(reservationDate, reservationTime, theme));
            CountDownLatch firstLocked = new CountDownLatch(1);
            CountDownLatch releaseFirstLock = new CountDownLatch(1);
            CountDownLatch secondLocked = new CountDownLatch(1);

            // when
            Future<?> first = executorService.submit(() -> lockSlotUntil(firstLocked,
                releaseFirstLock, reservationDate, reservationTime, theme));
            assertThat(firstLocked.await(1, TimeUnit.SECONDS)).isTrue();

            Future<?> second = executorService.submit(
                () -> transactionTemplate.executeWithoutResult(
                    status -> {
                        reservationSlotRepository.findByDateAndTimeAndThemeForUpdate(reservationDate, reservationTime, theme);
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

    private void lockSlotUntil(CountDownLatch firstLocked, CountDownLatch releaseFirstLock,
        ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        transactionTemplate.executeWithoutResult(status -> {
            reservationSlotRepository.findByDateAndTimeAndThemeForUpdate(reservationDate,
                reservationTime, theme);
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
