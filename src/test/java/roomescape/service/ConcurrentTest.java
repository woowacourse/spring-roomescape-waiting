package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.common.exception.ConflictException;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.command.WaitingCommand;

@SpringBootTest
public class ConcurrentTest {

    private static final String CANCELLER_NAME = "예약자";
    private static final String WAITER_NAME = "대기자";

    private final LocalDate date = LocalDate.parse("2026-05-12");
    private final Long timeId = 5L;
    private final Long themeId = 1L;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long reservationId;
    private Long waitingId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");

        SimpleJdbcInsert reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        SimpleJdbcInsert waitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> reservationParams = new HashMap<>();
        reservationParams.put("name", CANCELLER_NAME);
        reservationParams.put("date", date);
        reservationParams.put("time_id", timeId);
        reservationParams.put("theme_id", themeId);
        reservationId = reservationInsert.executeAndReturnKey(reservationParams).longValue();

        Map<String, Object> waitingParams = new HashMap<>();
        waitingParams.put("name", WAITER_NAME);
        waitingParams.put("date", date);
        waitingParams.put("time_id", timeId);
        waitingParams.put("theme_id", themeId);
        waitingParams.put("created_at", LocalDateTime.of(2026, 5, 10, 9, 0));
        waitingId = waitingInsert.executeAndReturnKey(waitingParams).longValue();
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
    }

    @RepeatedTest(value = 1000, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 동시_예약취소와_대기취소가_충돌해도_최종_데이터가_일관적이다() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.cancelReservation(reservationId, CANCELLER_NAME);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                finishGate.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGate.await();
                waitingService.delete(waitingId, WAITER_NAME);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                finishGate.countDown();
            }
        });

        startGate.countDown();
        boolean finished = finishGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished)
                .as("두 작업이 5초 안에 끝나야 함 — 무한 대기/데드락 의심")
                .isTrue();

        int reservationCount = countReservations();
        int waitingCount = countWaitings();

        assertThat(waitingCount)
                .as("최종 waiting은 어느 경로든 0건이어야 함")
                .isZero();
        assertThat(reservationCount)
                .as("최종 reservation은 0건(대기취소 우선) 또는 1건(승격) 둘 중 하나, 모순 상태 없음")
                .isIn(0, 1);

        List<Throwable> conflicts = errors.stream()
                .filter(e -> e instanceof ConflictException)
                .toList();
        assertThat(conflicts)
                .as("UNIQUE 충돌은 자동 X-lock + 명시 락 조합으로 막아야 함")
                .isEmpty();
    }

    @RepeatedTest(value = 1000, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 동시_사용자cancel과_관리자remove가_같은_예약에_호출돼도_데이터가_일관적이다() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.cancelReservation(reservationId, CANCELLER_NAME);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                finishGate.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.removeReservation(reservationId);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                finishGate.countDown();
            }
        });

        startGate.countDown();
        boolean finished = finishGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished)
                .as("두 작업이 5초 안에 끝나야 함")
                .isTrue();

        int reservationCount = countReservations();
        int waitingCount = countWaitings();

        assertThat(waitingCount)
                .as("W1이 reservation으로 승격됐어야 하므로 waiting은 0")
                .isZero();
        assertThat(reservationCount)
                .as("승격된 reservation 1건이 남아야 함")
                .isEqualTo(1);
        List<Throwable> conflicts = errors.stream()
                .filter(e -> e instanceof ConflictException)
                .toList();
        assertThat(conflicts)
                .as("UNIQUE 충돌은 자동 X-lock + 명시 락 조합으로 막아야 함")
                .isEmpty();
    }

    @RepeatedTest(value = 1000, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 동시_waiting신청과_예약취소가_충돌해도_dangling_waiting이_없다() throws Exception {
        jdbcTemplate.update("DELETE FROM waiting");

        String applicantName = "신청자";
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                startGate.await();
                waitingService.save(new WaitingCommand(applicantName, date, timeId, themeId));
            } catch (Throwable ignored) {
            } finally {
                finishGate.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.cancelReservation(reservationId, CANCELLER_NAME);
            } catch (Throwable ignored) {
            } finally {
                finishGate.countDown();
            }
        });

        startGate.countDown();
        boolean finished = finishGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished)
                .as("두 작업이 5초 안에 끝나야 함")
                .isTrue();

        int reservationCount = countReservations();
        int waitingCount = countWaitings();

        boolean dangling = reservationCount == 0 && waitingCount > 0;
        assertThat(dangling)
                .as("dangling waiting (예약 없는 슬롯에 대기만 떠 있는 상태) 발생 금지")
                .isFalse();
    }

    @RepeatedTest(value = 1000, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 동시_cancel과_admin_remove시_waiting이_2건이면_이중승격_race가_발생할_수_있다() throws Exception {
        SimpleJdbcInsert waitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
        Map<String, Object> w2Params = new HashMap<>();
        w2Params.put("name", "두번째대기자");
        w2Params.put("date", date);
        w2Params.put("time_id", timeId);
        w2Params.put("theme_id", themeId);
        w2Params.put("created_at", LocalDateTime.of(2026, 5, 10, 10, 0));
        waitingInsert.executeAndReturnKey(w2Params);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.cancelReservation(reservationId, CANCELLER_NAME);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                finishGate.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.removeReservation(reservationId);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                finishGate.countDown();
            }
        });

        startGate.countDown();
        boolean finished = finishGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished)
                .as("두 작업이 5초 안에 끝나야 함")
                .isTrue();

        int reservationCount = countReservations();

        assertThat(reservationCount)
                .as("UNIQUE 제약이 같은 슬롯의 reservation 중복을 막아야 함 (0 또는 1)")
                .isIn(0, 1);

        List<Throwable> uniqueViolations = errors.stream()
                .filter(e -> e instanceof DataIntegrityViolationException)
                .toList();
        assertThat(uniqueViolations)
                .as("이중 승격으로 인한 UNIQUE 위반이 없어야 함")
                .isEmpty();
    }

    @RepeatedTest(value = 1000, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 동시_changeReservationSlot과_새슬롯_waiting_신청() throws Exception {
        // setUp의 waiting 제거 — 변경 흐름의 promote(old slot) 트리거가 시나리오에 영향 주지 않게
        jdbcTemplate.update("DELETE FROM waiting");

        Long newTimeId = 6L;
        ReservationCommand changeCommand = new ReservationCommand(
                CANCELLER_NAME, date, newTimeId, themeId
        );
        WaitingCommand waitCommand = new WaitingCommand(
                "신청자", date, newTimeId, themeId
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.changeReservationSlot(reservationId, changeCommand);
            } catch (Throwable ignored) {
            } finally {
                finishGate.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGate.await();
                waitingService.save(waitCommand);
            } catch (Throwable ignored) {
            } finally {
                finishGate.countDown();
            }
        });

        startGate.countDown();
        boolean finished = finishGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished)
                .as("두 작업이 5초 안에 끝나야 함")
                .isTrue();

        int newSlotReservation = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, newTimeId, themeId
        );
        int newSlotWaiting = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, newTimeId, themeId
        );

        boolean dangling = newSlotReservation == 0 && newSlotWaiting > 0;
        assertThat(dangling)
                .as("새 슬롯에 dangling waiting (예약 없는데 대기만 있음) 발생 금지")
                .isFalse();
    }

    @RepeatedTest(value = 1000, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 동시_changeReservationSlot과_old슬롯_waiting_취소() throws Exception {
        Long newTimeId = 6L;
        ReservationCommand changeCommand = new ReservationCommand(
                CANCELLER_NAME, date, newTimeId, themeId
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                startGate.await();
                reservationService.changeReservationSlot(reservationId, changeCommand);
            } catch (Throwable ignored) {
            } finally {
                finishGate.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGate.await();
                waitingService.delete(waitingId, WAITER_NAME);
            } catch (Throwable ignored) {
            } finally {
                finishGate.countDown();
            }
        });

        startGate.countDown();
        boolean finished = finishGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished)
                .as("두 작업이 5초 안에 끝나야 함")
                .isTrue();

        int oldSlotWaiting = countWaitings();

        assertThat(oldSlotWaiting)
                .as("old 슬롯의 waiting은 promote되거나 사용자 취소로 사라져야 함 (0건)")
                .isZero();
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, timeId, themeId
        );
    }

    private int countWaitings() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, timeId, themeId
        );
    }
}