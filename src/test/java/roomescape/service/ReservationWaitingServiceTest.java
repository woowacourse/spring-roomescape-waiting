package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.exception.RoomEscapeException;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;
import roomescape.dao.ReservationDao;
import roomescape.domain.Reservation;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;

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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationTimeDao timeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationWaitingService reservationWaitingService;
    @Autowired
    private ReservationWaitingDao reservationWaitingDao;
    @Autowired
    private ReservationDao reservationDao;

    @Test
    void 예약_대기를_추가한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationDao.insert(Reservation.createWithoutId(
                "브라운", LocalDate.of(2026, 6, 10), time, theme
        ));
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );

        // when
        ReservationWaitingResponse response = reservationWaitingService.addReservationWaiting(command, LocalDateTime.of(2026, 6, 9, 10, 0)
        );

        // then
        assertThat(response)
                .extracting(ReservationWaitingResponse::name, ReservationWaitingResponse::reservationDate, waitingResponse -> waitingResponse.time().id(), waitingResponse -> waitingResponse.theme().id())
                .containsExactly("맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId());
    }

    @Test
    void 예약_대기를_취소한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId("브라운", LocalDateTime.now(), LocalDate.of(2026, 5, 5), time, theme);
        ReservationWaiting saved = reservationWaitingDao.insert(reservationWaiting);

        assertThatNoException().isThrownBy(() -> reservationWaitingService.delete(saved.getId()));
    }

    @Test
    void 존재하지_않는_시간으로_예약_대기를_신청하면_예외가_발생한다() {
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), 999L, theme.getId()
        );

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약_대기를_신청하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), 999L
        );

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약이_없는_슬롯에는_예약_대기를_신청할_수_없다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", LocalDate.of(2026, 6, 10), time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 동일한_예약_대기를_중복으로_신청하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);
        reservationDao.insert(Reservation.createWithoutId("브라운", date, time, theme));
        reservationWaitingDao.insert(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), date, time, theme));

        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", date, time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(command, LocalDateTime.of(2026, 6, 10, 10, 1)))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜_시간으로_예약_대기를_신청하면_예외가_발생한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);
        reservationDao.insert(Reservation.createWithoutId("브라운", date, time, theme));

        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                "맥스", date, time.getId(), theme.getId()
        );

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(
                command,
                LocalDateTime.of(2026, 6, 10, 10, 1)
        )).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 같은_슬롯에_대기를_신청하면_신청_순서대로_순번이_부여된다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);
        reservationDao.insert(Reservation.createWithoutId("브라운", date, time, theme));

        ReservationWaitingResponse first = reservationWaitingService.addReservationWaiting(
                new CreateReservationWaitingCommand("맥스", date, time.getId(), theme.getId()),
                LocalDateTime.of(2026, 6, 9, 10, 0)
        );
        ReservationWaitingResponse second = reservationWaitingService.addReservationWaiting(
                new CreateReservationWaitingCommand("로지", date, time.getId(), theme.getId()),
                LocalDateTime.of(2026, 6, 9, 11, 0)
        );

        assertThat(first.order()).isEqualTo(1);
        assertThat(second.order()).isEqualTo(2);
    }

    @Test
    void 이미_같은_슬롯에_예약한_사용자는_예약_대기를_신청할_수_없다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        reservationDao.insert(Reservation.createWithoutId("맥스", date, time, theme));

        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand("맥스", date, time.getId(), theme.getId());

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(command, LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 같은_슬롯에_동시에_대기를_신청해도_순번이_중복되지_않는다() throws InterruptedException {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 20);

        reservationDao.insert(Reservation.createWithoutId("브라운", date, time, theme));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<ReservationWaitingResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> unexpectedExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int index = i;

            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                            "사용자" + index,
                            date,
                            time.getId(),
                            theme.getId()
                    );

                    ReservationWaitingResponse response = reservationWaitingService.addReservationWaiting(
                            command,
                            LocalDateTime.of(2026, 6, 19, 10, 0)
                    );

                    responses.add(response);
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

        assertThat(unexpectedExceptions).isEmpty();
        assertThat(responses).hasSize(threadCount);
        assertThat(responses)
                .extracting(ReservationWaitingResponse::order)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }
}
