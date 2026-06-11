package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationWaitingErrorCode;
import roomescape.dao.*;
import roomescape.domain.*;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationSlotDao reservationSlotDao;

    @MockitoSpyBean
    private ReservationDao reservationDao;

    @MockitoSpyBean
    private ReservationWaitingDao waitingDao;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 예약을_추가한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().plusDays(1), time.getId(), theme.getId()
        );

        ReservationResponse response = reservationService.addReservation(command, LocalDateTime.now());

        assertThat(response)
                .extracting(ReservationResponse::name, ReservationResponse::date)
                .containsExactly("브라운", LocalDate.now().plusDays(1));
    }

    @Test
    void 존재하지_않는_시간으로_예약하면_예외가_발생한다() {
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().plusDays(1), 999L, theme.getId()
        );

        assertThatThrownBy(() -> reservationService.addReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().plusDays(1), time.getId(), 999L
        );

        assertThatThrownBy(() -> reservationService.addReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 중복_예약을_하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, theme);

        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", date, time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationService.addReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_예약하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationCommand command = new CreateReservationCommand(
                "브라운", LocalDate.now().minusDays(1), time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationService.addReservation(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 전체_예약을_조회한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme);
        saveReservation("로지", LocalDate.of(2026, 5, 6), time, theme);

        List<ReservationResponse> responses = reservationService.getAllReservations();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ReservationResponse::name).containsExactly("브라운", "로지");
    }

    @Test
    void 내_예약과_대기를_함께_조회한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://thumbnail1.com");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://thumbnail2.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 10), time, theme1);
        saveReservationWaiting("브라운", LocalDate.of(2026, 5, 10), time, theme2);

        List<MyReservationResponse> responses = reservationService.getMyReservations("브라운");

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(MyReservationResponse::status).containsExactly(ReservationStatus.RESERVED, ReservationStatus.WAITING);
    }

    @Test
    void 내_예약과_대기가_날짜_순으로_정렬된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://thumbnail1.com");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://thumbnail2.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 10), time, theme1);
        saveReservationWaiting("브라운", LocalDate.of(2026, 5, 5), time, theme2);

        List<MyReservationResponse> responses = reservationService.getMyReservations("브라운");

        assertThat(responses).extracting(MyReservationResponse::date).containsExactly(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time1, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().plusDays(2), time2.getId()
        );

        ReservationResponse response = reservationService.update(saved.getId(), command, LocalDateTime.now());

        assertThat(response.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().plusDays(1), time.getId()
        );

        assertThatThrownBy(() -> reservationService.update(999L, command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_변경하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().minusDays(1), time.getId()
        );

        assertThatThrownBy(() -> reservationService.update(saved.getId(), command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 중복된_날짜_시간으로_변경하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, theme);
        Reservation saved = saveReservation("로지", LocalDate.now().plusDays(2), time, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(date, time.getId());

        assertThatThrownBy(() -> reservationService.update(saved.getId(), command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);

        assertThatNoException().isThrownBy(() -> reservationService.delete(saved.getId()));
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.delete(999L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 내_예약_조회에서_대기_순번은_전체_대기열_기준으로_계산된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        saveReservation("예약자", date, time, theme);
        saveReservationWaiting("맥스", date, time, theme);
        saveReservationWaiting("로지", date, time, theme);
        saveReservationWaiting("브라운", date, time, theme);

        List<MyReservationResponse> responses = reservationService.getMyReservations("브라운");
        assertThat(responses.getFirst().order()).isEqualTo(3);
    }

    @Test
    void 예약_시간까지_24시간_미만이면_취소할_수_없다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation reservation = saveReservation(
                "브라운",
                LocalDate.of(2026, 6, 10),
                time,
                theme
        );

        LocalDateTime now = LocalDateTime.of(2026, 6, 10, 10, 1);

        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), now))
                .isInstanceOfSatisfying(RoomEscapeException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANNOT_CANCEL)
                );
    }

    @Test
    void 예약_시간까지_24시간_전이면_취소할_수_있다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation reservation = saveReservation(
                "브라운",
                LocalDate.of(2026, 6, 10),
                time,
                theme
        );

        LocalDateTime now = LocalDateTime.of(2026, 6, 9, 10, 0);

        reservationService.cancel(reservation.getId(), now);

        List<ReservationResponse> reservations = reservationService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @Test
    void 예약을_취소하면_1순위_대기가_예약으로_자동_전환된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        Reservation reservation = saveReservation("브라운", date, time, theme);
        saveReservationWaiting("맥스", date, time, theme);

        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 10, 0);

        reservationService.cancel(reservation.getId(), now);

        List<ReservationResponse> reservations = reservationService.getAllReservations();

        assertThat(reservations)
                .extracting(ReservationResponse::name)
                .containsExactly("맥스");
    }

    @Test
    void 예약을_취소하면_자동_전환된_대기는_대기_목록에서_삭제된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        Reservation reservation = saveReservation("브라운", date, time, theme);
        saveReservationWaiting("맥스", date, time, theme);

        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 10, 0);

        reservationService.cancel(reservation.getId(), now);

        List<MyReservationResponse> responses = reservationService.getMyReservations("맥스");

        assertThat(responses)
                .extracting(MyReservationResponse::status)
                .containsExactly(ReservationStatus.RESERVED);
    }

    @Test
    void 예약을_취소하면_남은_대기의_순번이_재계산된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        Reservation reservation = saveReservation("브라운", date, time, theme);
        saveReservationWaiting("맥스", date, time, theme);
        saveReservationWaiting("로지", date, time, theme);

        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 10, 0);

        reservationService.cancel(reservation.getId(), now);

        List<MyReservationResponse> responses = reservationService.getMyReservations("로지");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo(ReservationStatus.WAITING);
        assertThat(responses.getFirst().order()).isEqualTo(1);
    }

    @Test
    void 대기가_없는_예약을_취소하면_예약만_삭제된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation reservation = saveReservation(
                "브라운",
                LocalDate.of(2026, 6, 10),
                time,
                theme
        );

        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 10, 0);

        reservationService.cancel(reservation.getId(), now);
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @Test
    void 자동_승격_중_예약_생성이_실패하면_예약_취소도_롤백된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        Reservation reservation = saveReservation("브라운", date, time, theme);
        saveReservationWaiting("맥스", date, time, theme);

        doThrow(new RuntimeException("승격 실패"))
                .when(reservationDao)
                .insert(argThat(newReservation -> newReservation.getName().equals("맥스")));

        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 10, 0);

        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), now))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("승격 실패");

        List<ReservationResponse> reservations = reservationService.getAllReservations();

        assertThat(reservations)
                .extracting(ReservationResponse::name)
                .containsExactly("브라운");

        List<MyReservationResponse> waitingResponses = reservationService.getMyReservations("맥스");

        assertThat(waitingResponses).hasSize(1);
        assertThat(waitingResponses.getFirst().status()).isEqualTo(ReservationStatus.WAITING);
        assertThat(waitingResponses.getFirst().order()).isEqualTo(1);
    }

    @Test
    void 자동_승격중_대기_삭제가_실패하면_예약_취소와_예약_생성이_롤백된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        Reservation reservation = saveReservation("브라운", date, time, theme);
        ReservationWaiting waiting = saveReservationWaiting("맥스", date, time, theme);

        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(waitingDao)
                .delete(waiting.getId());

        LocalDateTime now = LocalDateTime.of(2026, 6, 8, 10, 0);

        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), now))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 삭제 실패");

        List<ReservationResponse> reservations = reservationService.getAllReservations();

        assertThat(reservations)
                .extracting(ReservationResponse::name)
                .containsExactly("브라운");

        List<MyReservationResponse> waitingResponses = reservationService.getMyReservations("맥스");

        assertThat(waitingResponses).hasSize(1);
        assertThat(waitingResponses.getFirst().status()).isEqualTo(ReservationStatus.WAITING);
        assertThat(waitingResponses.getFirst().order()).isEqualTo(1);

    }

    @Test
    void 같은_슬롯에_동시에_예약하면_하나만_성공한다() throws InterruptedException {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 20);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    CreateReservationCommand command = new CreateReservationCommand(
                            "사용자" + index,
                            date,
                            time.getId(),
                            theme.getId()
                    );

                    reservationService.addReservation(command, LocalDateTime.of(2026, 6, 19, 10, 0));
                    successCount.incrementAndGet();
                } catch (RoomEscapeException exception) {
                    if (exception.getErrorCode() == ReservationErrorCode.DUPLICATE) {
                        duplicateCount.incrementAndGet();
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(threadCount - 1);
        assertThat(reservationService.getAllReservations()).hasSize(1);
    }

    @Test
    void 예약_취소와_대기_취소가_동시에_실행되어도_대기가_중복_처리되지_않는다() throws InterruptedException {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 20);

        Reservation reservation = saveReservation("브라운", date, time, theme);
        ReservationWaiting waiting = saveReservationWaiting("맥스", date, time, theme);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger cancelSuccessCount = new AtomicInteger();
        AtomicInteger waitingDeleteSuccessCount = new AtomicInteger();
        AtomicInteger waitingNotFoundCount = new AtomicInteger();
        List<Throwable> unexpectedExceptions = Collections.synchronizedList(new ArrayList<>());

        executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                reservationService.cancel(reservation.getId(), LocalDateTime.of(2026, 6, 19, 10, 0));
                cancelSuccessCount.incrementAndGet();
            } catch (Throwable throwable) {
                unexpectedExceptions.add(throwable);
            } finally {
                doneLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                reservationWaitingService.delete(waiting.getId());
                waitingDeleteSuccessCount.incrementAndGet();
            } catch (RoomEscapeException exception) {
                if (exception.getErrorCode() == ReservationWaitingErrorCode.RESERVATION_WAITING_NOT_FOUND) {
                    waitingNotFoundCount.incrementAndGet();
                } else {
                    unexpectedExceptions.add(exception);
                }
            } catch (Throwable throwable) {
                unexpectedExceptions.add(throwable);
            } finally {
                doneLatch.countDown();
            }
        });

        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();

        List<ReservationResponse> reservations = reservationService.getAllReservations();
        List<MyReservationResponse> waitingResponses = reservationService.getMyReservations("맥스");

        assertThat(unexpectedExceptions).isEmpty();
        assertThat(cancelSuccessCount.get()).isEqualTo(1);
        assertThat(waitingDeleteSuccessCount.get() + waitingNotFoundCount.get()).isEqualTo(1);

        assertThat(reservations)
                .extracting(ReservationResponse::name)
                .doesNotContain("브라운");

        assertThat(reservations).hasSizeLessThanOrEqualTo(1);
        assertThat(reservations)
                .allSatisfy(response -> assertThat(response.name()).isEqualTo("맥스"));

        assertThat(waitingResponses)
                .filteredOn(response -> response.status() == ReservationStatus.WAITING)
                .isEmpty();
    }

    @Test
    void 서로_다른_예약을_같은_슬롯으로_동시에_변경하면_하나만_성공한다() throws InterruptedException {
        ReservationTime originalTime = saveTime(10, 0);
        ReservationTime targetTime = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");

        Reservation firstReservation = saveReservation(
                "브라운",
                LocalDate.of(2026, 6, 20),
                originalTime,
                theme
        );
        Reservation secondReservation = saveReservation(
                "로지",
                LocalDate.of(2026, 6, 21),
                originalTime,
                theme
        );

        LocalDate targetDate = LocalDate.of(2026, 6, 22);
        UpdateReservationCommand command = new UpdateReservationCommand(targetDate, targetTime.getId());

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        List<Throwable> unexpectedExceptions = Collections.synchronizedList(new ArrayList<>());

        List<Long> reservationIds = List.of(firstReservation.getId(), secondReservation.getId());

        for (Long reservationId : reservationIds) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    reservationService.update(
                            reservationId,
                            command,
                            LocalDateTime.of(2026, 6, 19, 10, 0)
                    );

                    successCount.incrementAndGet();
                } catch (RoomEscapeException exception) {
                    if (exception.getErrorCode() == ReservationErrorCode.DUPLICATE) {
                        duplicateCount.incrementAndGet();
                    } else {
                        unexpectedExceptions.add(exception);
                    }
                } catch (Throwable throwable) {
                    unexpectedExceptions.add(throwable);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();

        List<ReservationResponse> reservations = reservationService.getAllReservations();

        assertThat(unexpectedExceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(1);
        assertThat(reservations).hasSize(2);
        assertThat(reservations)
                .filteredOn(response ->
                        response.date().equals(targetDate)
                                && response.time().id().equals(targetTime.getId())
                )
                .hasSize(1);
    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        ReservationSlot slot = saveSlot(date, time, theme);
        return reservationDao.insert(Reservation.createWithoutId(name, slot));
    }

    private ReservationWaiting saveReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        ReservationSlot slot = saveSlot(date, time, theme);
        return waitingDao.insert(ReservationWaiting.createWithoutId(name, LocalDateTime.now(), slot));
    }

    private ReservationSlot saveSlot(LocalDate date, ReservationTime time, Theme theme) {
        return reservationSlotDao.findOrCreate(new ReservationSlot(date, time, theme));
    }
}
