package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;
import roomescape.test_config.fixture.SqlFixtureGenerator;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestClockConfig.class, SqlFixtureGenerator.class})
@Sql(value = "/acceptance-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SqlFixtureGenerator sqlFixtureGenerator;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private MutableClock clock;

    @Test
    @DisplayName("동시에 같은 날짜, 시간, 테마로 예약하면 확정 예약은 하나만 생성되어야 한다.")
    void create_concurrently_sameSlot_onlyOneConfirmed() throws Exception {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);

        // when
        executeConcurrently(
                () -> reservationService.create("브라운", date, time.getId(), theme.getId()),
                () -> reservationService.create("포비", date, time.getId(), theme.getId())
        );

        // then
        long confirmedCount = countConfirmedReservations(date, time.getId(), theme.getId());
        assertThat(confirmedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("서로 다른 두 사용자가 동시에 같은 슬롯으로 예약을 수정하면 확정 예약은 하나만 유지되어야 한다.")
    void editDateTime_concurrently_sameSlot_onlyOneConfirmed() throws Exception {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime targetTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        ReservationTime brownTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(11, 0));
        ReservationTime pobiTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate originalDate = LocalDate.of(2025, 5, 11);
        LocalDate targetDate = LocalDate.of(2025, 5, 12);

        Reservation brown = sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(originalDate, brownTime, theme),
                Status.CONFIRMED);
        Reservation pobi = sqlFixtureGenerator.insertReservation(
                "포비",
                sqlFixtureGenerator.insertReservationSlot(originalDate, pobiTime, theme),
                Status.CONFIRMED);

        // when
        executeConcurrently(
                () -> reservationService.editDateTime(brown.getId(), targetDate, targetTime.getId(), brown.getGuestName()),
                () -> reservationService.editDateTime(pobi.getId(), targetDate, targetTime.getId(), pobi.getGuestName())
        );

        // then
        assertThat(countReservationsByStatus(targetDate, targetTime.getId(), theme.getId(), Status.CONFIRMED)).isEqualTo(1);
        assertThat(countReservationsByStatus(targetDate, targetTime.getId(), theme.getId(), Status.WAITING)).isEqualTo(1);
    }

    @Test
    @DisplayName("확정 예약 취소와 같은 슬롯 예약 생성이 동시에 수행되어도 확정 예약은 하나 유지되어야 한다.")
    void cancel_concurrently_createSameSlot_keepOneConfirmed() throws Exception {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);
        Reservation reservation = sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(date, time, theme),
                Status.CONFIRMED);

        // when
        executeConcurrently(
                () -> reservationService.cancel(reservation.getId()),
                () -> reservationService.create("포비", date, time.getId(), theme.getId())
        );

        // then
        assertThat(countReservationsByStatus(date, time.getId(), theme.getId(), Status.CONFIRMED)).isEqualTo(1);
        assertThat(countReservationsByStatus(date, time.getId(), theme.getId(), Status.WAITING)).isZero();
    }

    @Test
    @DisplayName("확정 예약 수정과 기존 슬롯 예약 생성이 동시에 수행되어도 기존 슬롯의 확정 예약은 하나 유지되어야 한다.")
    void editDateTime_concurrently_createOriginalSlot_keepOneConfirmed() throws Exception {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime originalTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        ReservationTime targetTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate originalDate = LocalDate.of(2025, 5, 11);
        LocalDate targetDate = LocalDate.of(2025, 5, 12);
        Reservation reservation = sqlFixtureGenerator.insertReservation(
                "브라운",
                sqlFixtureGenerator.insertReservationSlot(originalDate, originalTime, theme),
                Status.CONFIRMED);

        // when
        executeConcurrently(
                () -> reservationService.editDateTime(
                        reservation.getId(), targetDate, targetTime.getId(), reservation.getGuestName()),
                () -> reservationService.create("포비", originalDate, originalTime.getId(), theme.getId())
        );

        // then
        assertThat(countReservationsByStatus(originalDate, originalTime.getId(), theme.getId(), Status.CONFIRMED))
                .isEqualTo(1);
        assertThat(countReservationsByStatus(originalDate, originalTime.getId(), theme.getId(), Status.WAITING))
                .isZero();
        assertThat(countReservationsByStatus(targetDate, targetTime.getId(), theme.getId(), Status.CONFIRMED))
                .isEqualTo(1);
    }

    private void executeConcurrently(Runnable first, Runnable second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<?> firstFuture = executor.submit(() -> {
                await(startLatch);
                first.run();
            });
            Future<?> secondFuture = executor.submit(() -> {
                await(startLatch);
                second.run();
            });

            startLatch.countDown();
            firstFuture.get(3, TimeUnit.SECONDS);
            secondFuture.get(3, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private long countConfirmedReservations(LocalDate date, Long timeId, Long themeId) {
        return countReservationsByStatus(date, timeId, themeId, Status.CONFIRMED);
    }

    private long countReservationsByStatus(LocalDate date, Long timeId, Long themeId, Status status) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation r
                        INNER JOIN reservation_slot s
                            ON r.slot_id = s.id
                        WHERE s.date = :date
                          AND s.time_id = :timeId
                          AND s.theme_id = :themeId
                          AND r.status = :status
                        """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", timeId)
                        .addValue("themeId", themeId)
                        .addValue("status", status.toString()),
                Long.class);

        return count == null ? 0 : count;
    }

}
