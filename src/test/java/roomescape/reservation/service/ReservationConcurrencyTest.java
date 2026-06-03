package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.stream.IntStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.service.fixture.ReservationServiceFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Sql(scripts = "/concurrency-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ReservationConcurrencyTest extends ReservationServiceFixture {

    @Autowired
    ReservationService reservationService;

    @Test
    @DisplayName("동시에 같은 슬롯을 예약하면 확정은 1건만 생성된다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void create_concurrent() throws Exception {
        // given
        timeManager.setFixed(LocalDate.of(2025, 5, 10));

        LocalDate date = LocalDate.of(2025, 5, 11);
        ReservationTime time = insertReservationTime(LocalTime.of(11, 30));
        Theme theme = insertTheme("레벨2 탈출", "설명", "thumbnail");

        int threadCount = 300;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        // when
        IntStreams.range(threadCount).forEach(i ->
                executor.submit(() -> {
                    try {
                        reservationService.create(
                                "guest" + i,
                                date,
                                time.getId(),
                                theme.getId()
                        );
                    } catch (Throwable e) {
                        errors.add(e);
                    } finally {
                        latch.countDown();
                    }
                })
        );

        latch.await();

        // then
        List<Reservation> reservations = reservationRepository.findAll(1, 500);

        assertThat(reservations.stream()
                .filter(Reservation::isConfirmed)
                .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("동시에 승급이 발생해도 확정 예약은 하나만 존재한다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void promote_concurrent() throws Exception {
        // given
        timeManager.setFixed(LocalDate.of(2025, 5, 10));

        LocalDate date = LocalDate.of(2025, 5, 11);
        ReservationTime time = insertReservationTime(LocalTime.of(11, 30));
        Theme theme = insertTheme("레벨2 탈출", "설명", "thumbnail");

        Reservation confirmed = insertReservation("A", date, time, theme, Status.CONFIRMED);
        Reservation waiting1 = insertReservation("B", date, time, theme, Status.WAITING);
        Reservation waiting2 = insertReservation("C", date, time, theme, Status.WAITING);
        Reservation waiting3 = insertReservation("D", date, time, theme, Status.WAITING);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        List<Long> ids = List.of(
                confirmed.getId(),
                waiting2.getId(),
                waiting3.getId()
        );

        // when
        ids.forEach(id ->
                executor.submit(() -> {
                    try {
                        reservationService.cancel(id);
                    } finally {
                        latch.countDown();
                    }
                })
        );

        latch.await();

        // then
        List<Reservation> reservations = reservationRepository.findAll(1, 10);

        assertThat(reservations.stream()
                .filter(Reservation::isConfirmed)
                .count())
                .isEqualTo(1);

        assertThat(reservationRepository.findById(waiting1.getId())
                .orElseThrow()
                .getStatus())
                .isEqualTo(Status.CONFIRMED);
    }
}
