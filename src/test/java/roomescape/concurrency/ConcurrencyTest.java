package roomescape.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.acceptance.AcceptanceTest;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.facade.ReceptionFacade;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

class ConcurrencyTest extends AcceptanceTest {

    private static final LocalDate FUTURE_DATE = LocalDate.of(2026, 5, 10);

    @Autowired
    private ReceptionFacade receptionFacade;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES ('테마', '설명', 'url.jpg')");
        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '10:00'", Long.class);
        themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마'", Long.class);
    }

    @Test
    void 빈_슬롯에_동시_예약_요청시_하나만_예약되고_나머지는_409를_받는다() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger slotJustTakenCount = new AtomicInteger(0);
        AtomicInteger unexpectedFailCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String name = "user" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    receptionFacade.save(new ServiceReservationCreateRequest(name, FUTURE_DATE, timeId, themeId));
                    successCount.incrementAndGet();
                } catch (RoomEscapeException e) {
                    if (e.code() == DomainErrorCode.SLOT_JUST_TAKEN) {
                        slotJustTakenCount.incrementAndGet();
                    } else {
                        unexpectedFailCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    unexpectedFailCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        int reservationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);

        assertThat(unexpectedFailCount.get()).isEqualTo(0);
        assertThat(reservationCount).isEqualTo(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(slotJustTakenCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    void 예약이_있는_슬롯에_동시_대기_요청시_최대_대기_인원_3명을_초과하지_않는다() throws InterruptedException {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "예약자", FUTURE_DATE, timeId, themeId);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String name = "waiter" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    receptionFacade.save(new ServiceReservationCreateRequest(name, FUTURE_DATE, timeId, themeId));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        int waitCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wait", Integer.class);

        assertThat(waitCount).isLessThanOrEqualTo(3);
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }

    @Test
    void 예약_취소와_대기_신청이_동시에_발생해도_고아_대기가_생기지_않는다() throws InterruptedException {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "예약자", FUTURE_DATE, timeId, themeId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        AtomicInteger unexpectedFailCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                receptionFacade.save(new ServiceReservationCreateRequest("대기자", FUTURE_DATE, timeId, themeId));
            } catch (RoomEscapeException e) {
                if (e.code() != DomainErrorCode.SLOT_JUST_TAKEN) {
                    unexpectedFailCount.incrementAndGet();
                }
            } catch (Exception e) {
                unexpectedFailCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                receptionFacade.deleteReservation(reservationId);
            } catch (Exception e) {
                unexpectedFailCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        int reservationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        int waitCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wait", Integer.class);

        assertThat(unexpectedFailCount.get()).isEqualTo(0);
        if (waitCount > 0) {
            assertThat(reservationCount).isGreaterThan(0);
        }
    }
}
