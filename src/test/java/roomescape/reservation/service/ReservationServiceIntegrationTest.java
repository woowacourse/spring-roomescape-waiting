package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.reservation.fixture.ReservationFixture.waitReservation;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest(showSql = false)
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationService reservationService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TestEntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        reservationService = new ReservationService(reservationSlotRepository, reservationRepository,
            reservationTimeRepository, reservationDateRepository, themeRepository);
    }

    private ReservationTime saveTime() {
        return reservationTimeRepository.save(ReservationTimeFixture.activeTime15());
    }

    private ReservationDate saveDate() {
        return reservationDateRepository.save(ReservationDateFixture.activeOneWeekLater());
    }

    private ReservationDate saveSecondDate() {
        return reservationDateRepository.save(ReservationDateFixture.activeTwoWeekLater());
    }

    private Theme saveTheme(String themeName) {
        return themeRepository.save(ThemeFixture.activeTheme(themeName));
    }

    private ReservationTime saveSecondTime() {
        return reservationTimeRepository.save(ReservationTimeFixture.activeTime16());
    }


    @Nested
    @DisplayName("getMyReservations 메서드는")
    class GetMyReservationsTest {


        @Test
        @DisplayName("내 예약을 조회한다")
        void 성공1() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";
            String name2 = "사람2";
            String name3 = "사람3";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(reservation(name1, date, time, theme));
            reservationRepository.save(
                waitReservation(name2, date, time, theme, 1L));
            reservationRepository.save(
                waitReservation(name3, date, time, theme, 2L));

            // when
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name3);

            // then
            assertThat(actual.getFirst().waitingTurn())
                .isEqualTo(2);
        }


        @Test
        @DisplayName("reserved 상태인 예약은 순번을 가지고 있지 않다.")
        void 성공2() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(reservation(name1, date, time, theme));

            // when
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name1);

            // then
            assertThat(actual.getFirst().waitingTurn())
                .isNull();
        }

        @Test
        @DisplayName("예약이 취소되면 해당 슬롯의 대기 순번이 재정렬된다")
        void 성공3() {
            // given
            String name1 = "name1";
            String name2 = "name2";
            String name3 = "name3";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme("theme1");

            Reservation reserved = reservationRepository.save(
                reservation(name1, date, time, theme));
            Reservation firstWaiting = reservationRepository.save(
                waitReservation(name2, date, time, theme, 1L));
            reservationRepository.save(waitReservation(name3, date, time, theme, 2L));

            Long beforeWaitingTurn = reservationService.readAllByName(name3)
                .getFirst()
                .waitingTurn();

            // when
            reservationService.cancel(reserved.getId(), name1);
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name3);
            Reservation promoted = reservationRepository.findById(firstWaiting.getId()).get();

            // then
            assertAll(
                () -> assertThat(beforeWaitingTurn).isEqualTo(2),
                () -> assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED),
                () -> assertThat(actual.getFirst().waitingTurn()).isEqualTo(1)
            );
        }
    }

    @Nested
    @DisplayName("changeSchedule 메서드는")
    class ChangeScheduleTest {

        @Test
        @DisplayName("확정 예약이 다른 슬롯으로 변경되면 이전 슬롯의 첫 번째 대기가 승격된다")
        void 성공1() {
            // given
            String reservedName = "예약자";
            String waitingName = "대기자";

            ReservationTime previousTime = saveTime();
            ReservationTime newTime = saveSecondTime();
            ReservationDate previousDate = saveDate();
            ReservationDate newDate = saveSecondDate();
            Theme theme = saveTheme("theme1");

            Reservation reserved = reservationRepository.save(
                reservation(reservedName, previousDate, previousTime, theme));
            Reservation waiting = reservationRepository.save(
                waitReservation(waitingName, previousDate, previousTime, theme, 1L));
            ReservationChangeCommand command = new ReservationChangeCommand(
                reserved.getId(), reservedName, newDate.getId(), newTime.getId());

            // when
            Reservation changed = reservationService.changeSchedule(command);

            // then
            Reservation promoted = reservationRepository.findById(waiting.getId()).get();
            assertAll(
                () -> assertThat(changed.getDate().getId()).isEqualTo(newDate.getId()),
                () -> assertThat(changed.getTime().getId()).isEqualTo(newTime.getId()),
                () -> assertThat(changed.getStatus()).isEqualTo(ReservationStatus.RESERVED),
                () -> assertThat(changed.getWaitingOrder()).isZero(),
                () -> assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED),
                () -> assertThat(promoted.getWaitingOrder()).isZero()
            );
        }

        @Test
        @DisplayName("확정 예약이 예약이 존재하는 슬롯으로 변경되면 새 슬롯 기준 대기 순서로 변경된다")
        void 성공2() {
            // given
            String reservedName = "예약자";
            String otherName = "다른 예약자";
            String waitingName = "기존 대기자";

            ReservationTime previousTime = saveTime();
            ReservationTime newTime = saveSecondTime();
            ReservationDate previousDate = saveDate();
            ReservationDate newDate = saveSecondDate();
            Theme theme = saveTheme("theme1");

            Reservation reserved = reservationRepository.save(
                reservation(reservedName, previousDate, previousTime, theme));
            reservationRepository.save(reservation(otherName, newDate, newTime, theme));
            reservationRepository.save(waitReservation(waitingName, newDate, newTime, theme, 1L));
            ReservationChangeCommand command = new ReservationChangeCommand(
                reserved.getId(), reservedName, newDate.getId(), newTime.getId());

            // when
            Reservation changed = reservationService.changeSchedule(command);

            // then
            assertAll(
                () -> assertThat(changed.getStatus()).isEqualTo(ReservationStatus.WAITING),
                () -> assertThat(changed.getWaitingOrder()).isEqualTo(2L)
            );
        }
    }

    @Nested
    @DisplayName("changeScheduleByManager 메서드는")
    class ChangeScheduleByManagerTest {

        @Test
        @DisplayName("확정 예약이 다른 슬롯으로 변경되면 이전 슬롯의 첫 번째 대기가 승격된다")
        void 성공1() {
            // given
            String reservedName = "예약자";
            String waitingName = "대기자";

            ReservationTime previousTime = saveTime();
            ReservationTime newTime = saveSecondTime();
            ReservationDate previousDate = saveDate();
            ReservationDate newDate = saveSecondDate();
            Theme theme = saveTheme("theme1");

            Reservation reserved = reservationRepository.save(
                reservation(reservedName, previousDate, previousTime, theme));
            Reservation waiting = reservationRepository.save(
                waitReservation(waitingName, previousDate, previousTime, theme, 1L));
            ReservationChangeCommand command = new ReservationChangeCommand(
                reserved.getId(), null, newDate.getId(), newTime.getId());

            // when
            Reservation changed = reservationService.changeScheduleByManager(command);

            // then
            Reservation promoted = reservationRepository.findById(waiting.getId()).get();
            assertAll(
                () -> assertThat(changed.getDate().getId()).isEqualTo(newDate.getId()),
                () -> assertThat(changed.getTime().getId()).isEqualTo(newTime.getId()),
                () -> assertThat(changed.getStatus()).isEqualTo(ReservationStatus.RESERVED),
                () -> assertThat(changed.getWaitingOrder()).isZero(),
                () -> assertThat(promoted.getStatus()).isEqualTo(ReservationStatus.RESERVED),
                () -> assertThat(promoted.getWaitingOrder()).isZero()
            );
        }

        @Test
        @DisplayName("확정 예약이 예약이 존재하는 슬롯으로 변경되면 새 슬롯 기준 대기 순서로 변경된다")
        void 성공2() {
            // given
            String reservedName = "예약자";
            String otherName = "다른 예약자";
            String waitingName = "기존 대기자";

            ReservationTime previousTime = saveTime();
            ReservationTime newTime = saveSecondTime();
            ReservationDate previousDate = saveDate();
            ReservationDate newDate = saveSecondDate();
            Theme theme = saveTheme("theme1");

            Reservation reserved = reservationRepository.save(
                reservation(reservedName, previousDate, previousTime, theme));
            reservationRepository.save(reservation(otherName, newDate, newTime, theme));
            reservationRepository.save(waitReservation(waitingName, newDate, newTime, theme, 1L));
            ReservationChangeCommand command = new ReservationChangeCommand(
                reserved.getId(), null, newDate.getId(), newTime.getId());

            // when
            Reservation changed = reservationService.changeScheduleByManager(command);

            // then
            assertAll(
                () -> assertThat(changed.getStatus()).isEqualTo(ReservationStatus.WAITING),
                () -> assertThat(changed.getWaitingOrder()).isEqualTo(2L)
            );
        }
    }

    @Nested
    @DisplayName("cancel 메서드는")
    class CancelTest {

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("확정 예약 취소와 1순위 대기 취소가 동시에 들어오면 승격 이후 예약을 취소한다")
        @Sql(scripts = "classpath:truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
        void 성공1() throws Exception {
            // given
            String reservedName = "예약자";
            String waitingName = "대기자";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme("theme1");

            Reservation reserved = reservationRepository.save(
                reservation(reservedName, date, time, theme));
            Reservation firstWaiting = reservationRepository.save(
                waitReservation(waitingName, date, time, theme, 1L));

            CountDownLatch reservedCancelLocked = new CountDownLatch(1);
            CountDownLatch completeReservedCancel = new CountDownLatch(1);
            ExecutorService executorService = Executors.newFixedThreadPool(2);

            try {
                Future<?> reservedCancel = executorService.submit(
                    () -> cancelReservedAndPromoteWaiting(reserved, reservedCancelLocked,
                        completeReservedCancel));
                assertThat(reservedCancelLocked.await(1, TimeUnit.SECONDS)).isTrue();

                Future<Reservation> waitingCancel = executorService.submit(
                    () -> transactionTemplate.execute(
                        status -> reservationService.cancel(firstWaiting.getId(), waitingName)));
                assertThatThrownBy(() -> waitingCancel.get(300, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);

                // when
                completeReservedCancel.countDown();
                reservedCancel.get(3, TimeUnit.SECONDS);

                // then
                Reservation canceled = waitingCancel.get(3, TimeUnit.SECONDS);
                Reservation actual = reservationRepository.findById(firstWaiting.getId()).get();
                assertAll(
                    () -> assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED),
                    () -> assertThat(actual.getStatus()).isEqualTo(ReservationStatus.CANCELED)
                );
            } finally {
                executorService.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("reserve 메서드는")
    class ReserveTest {


        @Test
        @DisplayName("같은 사람의 중복 슬롯에 대한 예약 요청 시 예외를 반환한다")
        void 실패() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(reservation(name1, date, time, theme));
            ReservationSaveCommand command = new ReservationSaveCommand(date.getId(), time.getId(),
                theme.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name1, command))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("동시에 같은 슬롯을 예약해도 하나만 예약되고 나머지는 대기된다")
        @Sql(scripts = "classpath:truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
        void 성공1() throws Exception {
            // given
            List<String> names = List.of("user1", "user2", "user3");
            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme("concurrency-theme");
            ReservationSaveCommand command = new ReservationSaveCommand(date.getId(), time.getId(),
                theme.getId());
            CountDownLatch ready = new CountDownLatch(names.size());
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executorService = Executors.newFixedThreadPool(names.size());

            try {
                List<Future<Reservation>> futures = names.stream()
                    .map(name -> executorService.submit(() -> reserveAfterStart(name, command,
                        ready, start)))
                    .toList();
                assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();

                // when
                start.countDown();
                for (Future<Reservation> future : futures) {
                    future.get(3, TimeUnit.SECONDS);
                }

                // then
                List<Reservation> actual = reservationRepository.findAllActiveByDateAndTimeAndTheme(
                    date, time, theme);

                assertAll(
                    () -> assertThat(actual).hasSize(names.size()),
                    () -> assertThat(actual)
                        .filteredOn(
                            reservation -> reservation.getStatus() == ReservationStatus.RESERVED)
                        .hasSize(1),
                    () -> assertThat(actual)
                        .filteredOn(
                            reservation -> reservation.getStatus() == ReservationStatus.WAITING)
                        .hasSize(names.size() - 1)
                );
            } finally {
                executorService.shutdownNow();
            }
        }
    }

    private Reservation reserveAfterStart(String name, ReservationSaveCommand command,
        CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        return transactionTemplate.execute(status -> reservationService.reserve(name, command));
    }

    private void cancelReservedAndPromoteWaiting(Reservation reserved,
        CountDownLatch reservedCancelLocked, CountDownLatch completeReservedCancel) {
        transactionTemplate.executeWithoutResult(status -> {
            lockSlot(reserved.getDate(), reserved.getTime(), reserved.getTheme());
            reservedCancelLocked.countDown();
            await(completeReservedCancel);

            Reservation reservationToCancel = reservationRepository.findById(reserved.getId())
                .get();
            reservationToCancel.cancel(reservationToCancel.getName());
            reservationRepository.findFirstWaitingByDateAndTimeAndTheme(
                    reservationToCancel.getDate(),
                    reservationToCancel.getTime(),
                    reservationToCancel.getTheme())
                .ifPresent(waiting -> {
                    waiting.changeToReserved();
                    reservationRepository.save(waiting);
                });
        });
    }

    private void lockSlot(ReservationDate reservationDate, ReservationTime reservationTime,
        Theme theme) {
        reservationSlotRepository.saveIfAbsent(reservationDate, reservationTime, theme);
        reservationSlotRepository.findByDateAndTimeAndThemeForUpdate(reservationDate, reservationTime,
            theme);
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
