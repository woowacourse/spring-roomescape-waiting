package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.controller.dto.UserReservationRequest;
import roomescape.domain.Member;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.MemberDao;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationConcurrencyTest {

    private static final long TIME_ID = 1L;
    private static final long THEME_ID = 1L;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM schedule");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");

        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)", TIME_ID, "10:00");
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url, price) VALUES (?, ?, ?, ?, ?)",
                THEME_ID,
                "테스트 테마",
                "테스트 설명",
                "https://example.com/theme.png",
                20000
        );
    }

    @DisplayName("같은 사용자가 같은 스케줄을 동시에 예약하면 하나만 성공한다.")
    @Test
    void sameMemberCannotReserveSameScheduleConcurrently() throws Exception {
        LocalDate date = LocalDate.now().plusDays(30);
        Member member = saveMember("member-a", "러로");
        UserReservationRequest request = new UserReservationRequest(date, TIME_ID, THEME_ID);

        List<Throwable> failures = executeConcurrently(
                () -> reservationService.saveReservationByMember(request, member),
                () -> reservationService.saveReservationByMember(request, member)
        );

        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst())
                .isInstanceOfSatisfying(RoomescapeException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION)
                );

        long scheduleId = findScheduleId(date);
        assertThat(countByScheduleAndStatus(scheduleId)).containsExactlyInAnyOrderEntriesOf(Map.of(
                ReservationStatus.RESERVED.name(), 1
        ));
    }

    @DisplayName("서로 다른 사용자가 같은 빈 슬롯을 동시에 예약하면 RESERVED는 하나만 생성되고 나머지는 WAITING이 된다.")
    @Test
    void concurrentSavesCreateSingleReservedReservation() throws Exception {
        LocalDate date = LocalDate.now().plusDays(31);
        Member first = saveMember("member-a", "러로");
        Member second = saveMember("member-b", "현미밥");
        UserReservationRequest request = new UserReservationRequest(date, TIME_ID, THEME_ID);

        List<Throwable> failures = executeConcurrently(
                () -> reservationService.saveReservationByMember(request, first),
                () -> reservationService.saveReservationByMember(request, second)
        );

        assertThat(failures).isEmpty();

        long scheduleId = findScheduleId(date);
        assertThat(countByScheduleAndStatus(scheduleId)).containsExactlyInAnyOrderEntriesOf(Map.of(
                ReservationStatus.RESERVED.name(), 1,
                ReservationStatus.WAITING.name(), 1
        ));
    }

    @DisplayName("같은 예약을 동시에 두 번 취소해도 대기 승격은 한 번만 일어난다.")
    @Test
    void concurrentCancelsPromoteWaitingReservationOnce() throws Exception {
        LocalDate date = LocalDate.now().plusDays(32);
        Member reserver = saveMember("member-a", "러로");
        Member firstWaiting = saveMember("member-b", "현미밥");
        Member secondWaiting = saveMember("member-c", "오뚜기밥");
        UserReservationRequest request = new UserReservationRequest(date, TIME_ID, THEME_ID);
        long reservationId = reservationService.saveReservationByMember(request, reserver);
        reservationService.saveReservationByMember(request, firstWaiting);
        reservationService.saveReservationByMember(request, secondWaiting);

        List<Throwable> failures = executeConcurrently(
                () -> reservationService.cancelReservation(reservationId, reserver),
                () -> reservationService.cancelReservation(reservationId, reserver)
        );

        assertThat(failures).isEmpty();

        long scheduleId = findScheduleId(date);
        assertThat(countByScheduleAndStatus(scheduleId)).containsExactlyInAnyOrderEntriesOf(Map.of(
                ReservationStatus.CANCELED.name(), 1,
                ReservationStatus.RESERVED.name(), 1,
                ReservationStatus.WAITING.name(), 1
        ));
    }

    @DisplayName("취소와 저장이 동시에 실행되어도 승격된 예약 외에 RESERVED가 추가로 생기지 않는다.")
    @Test
    void concurrentCancelAndSaveKeepSingleReservedReservation() throws Exception {
        LocalDate date = LocalDate.now().plusDays(33);
        Member reserver = saveMember("member-a", "러로");
        Member waiting = saveMember("member-b", "현미밥");
        Member newMember = saveMember("member-c", "오뚜기밥");
        UserReservationRequest request = new UserReservationRequest(date, TIME_ID, THEME_ID);
        long reservationId = reservationService.saveReservationByMember(request, reserver);
        reservationService.saveReservationByMember(request, waiting);

        List<Throwable> failures = executeConcurrently(
                () -> reservationService.cancelReservation(reservationId, reserver),
                () -> reservationService.saveReservationByMember(request, newMember)
        );

        assertThat(failures).isEmpty();

        long scheduleId = findScheduleId(date);
        assertThat(countByScheduleAndStatus(scheduleId)).containsExactlyInAnyOrderEntriesOf(Map.of(
                ReservationStatus.CANCELED.name(), 1,
                ReservationStatus.RESERVED.name(), 1,
                ReservationStatus.WAITING.name(), 1
        ));
    }

    @SafeVarargs
    private List<Throwable> executeConcurrently(ThrowingRunnable... tasks) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch ready = new CountDownLatch(tasks.length);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (ThrowingRunnable task : tasks) {
            futures.add(executorService.submit(toCallable(task, ready, start)));
        }

        ready.await();
        start.countDown();

        List<Throwable> failures = new ArrayList<>();
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                failures.add(e.getCause());
            }
        }

        executorService.shutdown();
        return failures;
    }

    private Callable<Void> toCallable(
            ThrowingRunnable task,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await();
            task.run();
            return null;
        };
    }

    private Member saveMember(String loginId, String name) {
        Long id = memberDao.save(new Member(null, loginId, name, "password", Role.USER));
        return memberDao.findById(id).orElseThrow();
    }

    private long findScheduleId(LocalDate date) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM schedule WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                TIME_ID,
                THEME_ID
        );
    }

    private Map<String, Integer> countByScheduleAndStatus(long scheduleId) {
        return jdbcTemplate.query(
                """
                        SELECT status, COUNT(*) AS count
                        FROM reservation
                        WHERE schedule_id = ?
                        GROUP BY status
                        """,
                rs -> {
                    Map<String, Integer> counts = new java.util.HashMap<>();
                    while (rs.next()) {
                        counts.put(rs.getString("status"), rs.getInt("count"));
                    }
                    return counts;
                },
                scheduleId
        );
    }

    @FunctionalInterface
    private interface ThrowingRunnable {

        void run() throws Exception;
    }
}
