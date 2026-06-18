package roomescape.domain.reservation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.exception.GeneralException;

@SpringBootTest
class ReservationTransactionIntegrationTest {

    private static final String DATABASE_URL = "jdbc:h2:mem:reservation_transaction_" + System.nanoTime();

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DATABASE_URL);
    }

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean(reset = MockReset.AFTER)
    private ReservationRepository reservationRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void 대기_생성_트랜잭션이_활성_예약을_잠그면_동시_취소는_커밋_후_진행된다() throws Exception {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = saveTime(LocalTime.of(10, 0));
        Theme theme = saveTheme("테마1");
        ReservationCreateResponseDto active = saveReservation("예약자", date, time, theme);

        CountDownLatch waitingSaved = new CountDownLatch(1);
        CountDownLatch releaseWaitingTransaction = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<ReservationCreateResponseDto> waitingFuture = executorService.submit(() ->
                transactionTemplate.execute(status -> {
                    ReservationCreateResponseDto waiting = saveWaitingReservation("대기자", date, time, theme);
                    waitingSaved.countDown();
                    awaitLatch(releaseWaitingTransaction);
                    return waiting;
                })
            );

            assertThat(waitingSaved.await(2, TimeUnit.SECONDS)).isTrue();

            Future<ReservationCancelResponseDto> cancelFuture = executorService.submit(
                () -> reservationService.cancelReservation(active.id(), "예약자"));

            assertThatThrownBy(() -> cancelFuture.get(300, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);

            releaseWaitingTransaction.countDown();
            ReservationCreateResponseDto waiting = waitingFuture.get(2, TimeUnit.SECONDS);
            cancelFuture.get(2, TimeUnit.SECONDS);

            // then
            assertThat(statusOf(active.id())).isEqualTo(ReservationStatus.CANCELED);
            assertThat(statusOf(waiting.id())).isEqualTo(ReservationStatus.ACTIVE);
        } finally {
            releaseWaitingTransaction.countDown();
            executorService.shutdownNow();
        }
    }

    @Test
    void 대기_승인_중_첫_번째_대기_취소가_동시에_들어오면_승인된_예약이_취소되지_않는다() throws Exception {
        // given
        LocalDate date = LocalDate.of(2099, 5, 2);
        Time time = saveTime(LocalTime.of(10, 0));
        Theme theme = saveTheme("테마1");
        ReservationCreateResponseDto active = saveReservation("예약자", date, time, theme);
        ReservationCreateResponseDto firstWaiting = saveWaitingReservation("대기자1", date, time, theme);
        ReservationCreateResponseDto secondWaiting = saveWaitingReservation("대기자2", date, time, theme);

        CountDownLatch approvingWaiting = new CountDownLatch(1);
        CountDownLatch releaseApproval = new CountDownLatch(1);
        doAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            if (reservation.getId().equals(firstWaiting.id())
                && reservation.getStatus() == ReservationStatus.ACTIVE) {
                approvingWaiting.countDown();
                awaitLatch(releaseApproval);
            }
            return invocation.callRealMethod();
        }).when(reservationRepository).update(any(Reservation.class));

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<ReservationCancelResponseDto> activeCancelFuture = executorService.submit(
                () -> reservationService.cancelReservation(active.id(), "예약자"));
            assertThat(approvingWaiting.await(2, TimeUnit.SECONDS)).isTrue();

            Future<ReservationCancelResponseDto> waitingCancelFuture = executorService.submit(
                () -> reservationService.cancelWaitingReservation(firstWaiting.id(), "대기자1"));
            assertThatThrownBy(() -> waitingCancelFuture.get(300, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);

            releaseApproval.countDown();
            activeCancelFuture.get(2, TimeUnit.SECONDS);

            assertThatThrownBy(() -> waitingCancelFuture.get(2, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(GeneralException.class)
                .hasMessage("대기중인 예약이 아닙니다.");
        } finally {
            releaseApproval.countDown();
            executorService.shutdownNow();
        }

        // then
        assertThat(statusOf(active.id())).isEqualTo(ReservationStatus.CANCELED);
        assertThat(statusOf(firstWaiting.id())).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(statusOf(secondWaiting.id())).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 같은_사람이_같은_슬롯에_대기_예약을_동시에_생성하면_하나만_성공한다() throws Exception {
        // given
        LocalDate date = LocalDate.of(2099, 5, 3);
        Time time = saveTime(LocalTime.of(10, 0));
        Theme theme = saveTheme("테마1");
        saveReservation("예약자", date, time, theme);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> firstFuture = executorService.submit(
                () -> saveWaitingAfterStart(start, "대기자", date, time, theme));
            Future<Boolean> secondFuture = executorService.submit(
                () -> saveWaitingAfterStart(start, "대기자", date, time, theme));

            start.countDown();

            List<Boolean> results = List.of(
                firstFuture.get(2, TimeUnit.SECONDS),
                secondFuture.get(2, TimeUnit.SECONDS)
            );

            // then
            assertThat(results).containsExactlyInAnyOrder(true, false);
            assertThat(countReservations("대기자", date, time, theme, ReservationStatus.WAITING)).isEqualTo(1);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void 예약_수정으로_기존_슬롯이_비면_첫_번째_대기_예약이_승인된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 4);
        Time oldTime = saveTime(LocalTime.of(10, 0));
        Time newTime = saveTime(LocalTime.of(11, 0));
        Theme theme = saveTheme("테마1");
        ReservationCreateResponseDto active = saveReservation("예약자", date, oldTime, theme);
        ReservationCreateResponseDto waiting = saveWaitingReservation("대기자", date, oldTime, theme);

        // when
        reservationService.updateReservation(
            active.id(),
            "예약자",
            new ReservationUpdateCommand(date, newTime.getId(), theme.getId(), versionOf(active.id()))
        );

        // then
        assertThat(statusOf(active.id())).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(timeIdOf(active.id())).isEqualTo(newTime.getId());
        assertThat(statusOf(waiting.id())).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(timeIdOf(waiting.id())).isEqualTo(oldTime.getId());
    }

    @Test
    void 대기_승인_중_예외가_발생하면_활성_예약_취소도_롤백된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 5);
        Time time = saveTime(LocalTime.of(10, 0));
        Theme theme = saveTheme("테마1");
        ReservationCreateResponseDto active = saveReservation("예약자", date, time, theme);
        ReservationCreateResponseDto waiting = saveWaitingReservation("대기자", date, time, theme);

        doAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            if (reservation.getStatus() == ReservationStatus.ACTIVE
                && reservation.getName().equals("대기자")) {
                throw new IllegalStateException("approval failed");
            }
            return invocation.callRealMethod();
        }).when(reservationRepository).update(any(Reservation.class));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelReservation(active.id(), "예약자"))
            .isInstanceOf(InvalidDataAccessApiUsageException.class)
            .hasMessage("approval failed");

        assertThat(statusOf(active.id())).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(statusOf(waiting.id())).isEqualTo(ReservationStatus.WAITING);
    }

    private Time saveTime(LocalTime startAt) {
        return timeRepository.save(Time.create(startAt));
    }

    private Theme saveTheme(String name) {
        return themeRepository.save(Theme.create(name, "설명", "image.png"));
    }

    private ReservationCreateResponseDto saveReservation(String name, LocalDate date, Time time, Theme theme) {
        return reservationService.saveReservation(new ReservationCreateCommand(
            name, date, time.getId(), theme.getId()));
    }

    private ReservationCreateResponseDto saveWaitingReservation(String name, LocalDate date, Time time, Theme theme) {
        return reservationService.saveWaitingReservation(new ReservationCreateCommand(
            name, date, time.getId(), theme.getId()));
    }

    private boolean saveWaitingAfterStart(
        CountDownLatch start, String name, LocalDate date, Time time, Theme theme) {
        awaitLatch(start);
        try {
            saveWaitingReservation(name, date, time, theme);
            return true;
        } catch (GeneralException exception) {
            assertThat(exception.getMessage()).isEqualTo("이미 대기 중인 이름, 날짜, 시간, 테마입니다.");
            return false;
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test latch");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private ReservationStatus statusOf(Long reservationId) {
        String status = jdbcTemplate.queryForObject(
            "SELECT status FROM reservation WHERE id = ?",
            String.class,
            reservationId
        );
        return ReservationStatus.valueOf(status);
    }

    private Long timeIdOf(Long reservationId) {
        return jdbcTemplate.queryForObject(
            "SELECT time_id FROM reservation WHERE id = ?",
            Long.class,
            reservationId
        );
    }

    private Long versionOf(Long reservationId) {
        return jdbcTemplate.queryForObject(
            "SELECT version FROM reservation WHERE id = ?",
            Long.class,
            reservationId
        );
    }

    private Integer countReservations(
        String name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM reservation
                WHERE name = ?
                  AND date = ?
                  AND time_id = ?
                  AND theme_id = ?
                  AND status = ?
                  AND deleted_at IS NULL
                """,
            Integer.class,
            name,
            date,
            time.getId(),
            theme.getId(),
            status.name()
        );
    }
}
