package roomescape.reservation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.slot.domain.ReservationSlot;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static roomescape.reservation.domain.ReservationStatus.RESERVED;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(ReservationService.class)
class ReservationServiceConcurrentTest extends ServiceSupport {

    private final String name = "송송";
    private final String themeName = "테마1";

    private ReservationDate date1;
    private ReservationTime time1;
    private ReservationDate date2;
    private ReservationTime time2;
    private ReservationSlot slot1;
    private ReservationSlot slot2;
    private Theme theme;
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        date1 = saveDate(ReservationDateFixture.oneWeekLater());
        time1 = saveTime(ReservationTimeFixture.activeTime15());
        date2 = saveDate(ReservationDateFixture.twoWeeksLater());
        time2 = saveTime(ReservationTimeFixture.activeTime16());
        theme = saveTheme(themeName);
        slot1 = saveSlot(ReservationSlot.of(date1, time1, theme));
        slot2 = saveSlot(ReservationSlot.of(date2, time2, theme));
    }

    @Test
    @DisplayName("동시 예약요청시 하나는 예약, 나머지는 대기로 들어간다.")
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reserve_concurrent() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(5);
        ReservationSaveCommand command = new ReservationSaveCommand(date1.getId(), time1.getId(), theme.getId());

        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.reserve(UUID.randomUUID().toString(), command);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // then
        List<Reservation> reservations =
                reservationRepository.findReservedAndWaitingBySlot(slot1);

        // when
        Assertions.assertThat(reservations)
                .hasSize(5);

        Assertions.assertThat(reservations)
                .filteredOn(reservation -> reservation.getStatus() == RESERVED)
                .hasSize(1);

        Assertions.assertThat(reservations)
                .filteredOn(reservation -> reservation.getStatus() == ReservationStatus.WAITING)
                .hasSize(4);
    }

    @Test
    @DisplayName("내가 동시 예약요청시 하나는 예약, 나머지는 실패가 된다.")
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reserve_concurrent_myself() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(5);
        ReservationSaveCommand command = new ReservationSaveCommand(date1.getId(), time1.getId(), theme.getId());

        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.reserve(name, command);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // then
        List<Reservation> reservations =
                reservationRepository.findReservedAndWaitingBySlot(slot1);

        // when
        Assertions.assertThat(reservations)
                .hasSize(1);

        Reservation myReservation = reservations.getFirst();
        Assertions.assertThat(myReservation.getName())
                .isEqualTo(name);
        Assertions.assertThat(myReservation.getStatus())
                .isEqualTo(RESERVED);
    }

}
