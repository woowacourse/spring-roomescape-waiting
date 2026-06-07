package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.fixture.DbFixtures;

@JdbcTest
@Import(ReservationJdbcRepository.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationJdbcRepositoryConcurrencyTest {

    private ExecutorService executorService;

    @Autowired
    private ReservationJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Test
    void findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate_첫번째_대기_삭제를_대기시킨다()
            throws Exception {
        WaitingSlot slot = insertWaitingSlot();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        executorService = Executors.newFixedThreadPool(2);

        Future<Reservation> lockFuture = executorService.submit(() -> transactionTemplate().execute(status -> {
            Reservation firstWaiting = repository.findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate(
                    slot.date(), slot.timeId(), slot.themeId(), slot.storeId()).orElseThrow();
            locked.countDown();
            await(release);
            return firstWaiting;
        }));
        assertThat(locked.await(1, TimeUnit.SECONDS)).isTrue();

        Future<Integer> deleteFuture = executorService.submit(
                () -> transactionTemplate().execute(status -> repository.deleteById(slot.firstWaitingId())));

        Thread.sleep(200);
        assertThat(deleteFuture.isDone()).isFalse();

        release.countDown();

        assertThat(lockFuture.get(1, TimeUnit.SECONDS).getId()).isEqualTo(slot.firstWaitingId());
        assertThat(deleteFuture.get(1, TimeUnit.SECONDS)).isEqualTo(1);
    }

    @Test
    void findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate_일반_조회는_대기시키지_않는다()
            throws Exception {
        WaitingSlot slot = insertWaitingSlot();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        executorService = Executors.newFixedThreadPool(2);

        Future<Reservation> lockFuture = executorService.submit(() -> transactionTemplate().execute(status -> {
            Reservation firstWaiting = repository.findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate(
                    slot.date(), slot.timeId(), slot.themeId(), slot.storeId()).orElseThrow();
            locked.countDown();
            await(release);
            return firstWaiting;
        }));
        assertThat(locked.await(1, TimeUnit.SECONDS)).isTrue();

        Future<Integer> selectFuture = executorService.submit(() -> jdbcTemplate.queryForObject(
                "select count(1) from reservation where id = ?", Integer.class, slot.firstWaitingId()));

        assertThat(selectFuture.get(1, TimeUnit.SECONDS)).isEqualTo(1);

        release.countDown();

        assertThat(lockFuture.get(1, TimeUnit.SECONDS).getId()).isEqualTo(slot.firstWaitingId());
    }

    private WaitingSlot insertWaitingSlot() {
        Long brown = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long charles = DbFixtures.insertMember(jdbcTemplate, "샤를");
        Long aron = DbFixtures.insertMember(jdbcTemplate, "아론");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long storeId = DbFixtures.defaultStoreId(jdbcTemplate);
        LocalDate date = LocalDate.of(2026, 6, 1);
        DbFixtures.insertReservation(jdbcTemplate, brown, themeId, date.toString(), timeId, storeId,
                ReservationStatus.RESERVED.name());
        Long firstWaitingId = DbFixtures.insertReservation(jdbcTemplate, charles, themeId, date.toString(), timeId,
                storeId, ReservationStatus.WAITING.name());
        Long secondWaitingId = DbFixtures.insertReservation(jdbcTemplate, aron, themeId, date.toString(), timeId,
                storeId, ReservationStatus.WAITING.name());
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 08:00:00",
                firstWaitingId);
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 09:00:00",
                secondWaitingId);
        return new WaitingSlot(date, themeId, timeId, storeId, firstWaitingId, secondWaitingId);
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }

    private void await(CountDownLatch latch) {
        try {
            assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private record WaitingSlot(
            LocalDate date,
            Long themeId,
            Long timeId,
            Long storeId,
            Long firstWaitingId,
            Long secondWaitingId
    ) {
    }
}
