package roomescape.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reservationwaiting.ReservationWaitingService;
import roomescape.service.theme.ThemeService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:unique-constraint-concurrency")
class UniqueConstraintConcurrencyTest {

    private static final int REQUEST_COUNT = 5;
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.parse("2026-08-05T12:00:00");

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clearTables();
    }

    @Test
    @DisplayName("동시에 같은 슬롯에 예약하면 하나만 성공하고 나머지는 충돌 처리된다")
    void createReservationConcurrently() throws Exception {
        Theme theme = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = reservationTimeService.save(LocalTime.parse("10:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        reservationSlotRepository.save(new ReservationSlot(date, theme, time));

        List<Throwable> results = runConcurrently(REQUEST_COUNT, index ->
                reservationService.save("예약자" + index, date, theme.getId(), time.getId(), REQUESTED_AT)
        );

        assertThat(results).filteredOn(throwable -> throwable == null).hasSize(1);
        assertThat(countConflict(results, ErrorCode.RESERVATION_DUPLICATED)).isEqualTo(REQUEST_COUNT - 1);
        assertThat(countRows("reservation")).isOne();
    }

    @Test
    @DisplayName("동시에 같은 이름으로 대기하면 하나만 성공하고 나머지는 충돌 처리된다")
    void createReservationWaitingWithSameNameConcurrently() throws Exception {
        Theme theme = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = reservationTimeService.save(LocalTime.parse("10:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        reservationSlotRepository.save(new ReservationSlot(date, theme, time));
        reservationService.save("예약자", date, theme.getId(), time.getId(), REQUESTED_AT);

        List<Throwable> results = runConcurrently(REQUEST_COUNT, index ->
                reservationWaitingService.save("아루", date, theme.getId(), time.getId(), REQUESTED_AT)
        );

        assertThat(results).filteredOn(throwable -> throwable == null).hasSize(1);
        assertThat(countConflict(results, ErrorCode.RESERVATION_WAITING_DUPLICATED)).isEqualTo(REQUEST_COUNT - 1);
        assertThat(countRows("reservation_waiting")).isOne();
    }

    @Test
    @DisplayName("동시에 서로 다른 이름으로 대기하면 모두 성공한다")
    void createReservationWaitingWithDifferentNamesConcurrently() throws Exception {
        Theme theme = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = reservationTimeService.save(LocalTime.parse("10:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        reservationSlotRepository.save(new ReservationSlot(date, theme, time));
        reservationService.save("예약자", date, theme.getId(), time.getId(), REQUESTED_AT);

        List<Throwable> results = runConcurrently(REQUEST_COUNT, index ->
                reservationWaitingService.save("대기자" + index, date, theme.getId(), time.getId(), REQUESTED_AT)
        );

        assertThat(results).allMatch(throwable -> throwable == null);
        assertThat(countRows("reservation_waiting")).isEqualTo(REQUEST_COUNT);
    }

    private List<Throwable> runConcurrently(final int requestCount, final ConcurrentOperation operation)
            throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Throwable>> futures = IntStream.range(0, requestCount)
                    .mapToObj(index -> executorService.submit(() -> {
                        ready.countDown();
                        start.await();
                        try {
                            operation.run(index);
                            return null;
                        } catch (Throwable throwable) {
                            return throwable;
                        }
                    }))
                    .toList();

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            return futures.stream()
                    .map(this::getResult)
                    .toList();
        } finally {
            executorService.shutdownNow();
        }
    }

    private Throwable getResult(final Future<Throwable> future) {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception exception) {
            return exception;
        }
    }

    private long countConflict(final List<Throwable> results, final ErrorCode errorCode) {
        return results.stream()
                .filter(ConflictException.class::isInstance)
                .map(ConflictException.class::cast)
                .filter(exception -> exception.getCode().equals(errorCode.getCode()))
                .count();
    }

    private int countRows(final String tableName) {
        return jdbcTemplate.queryForObject("SELECT count(1) FROM " + tableName, Integer.class);
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @FunctionalInterface
    private interface ConcurrentOperation {
        void run(int index);
    }
}
