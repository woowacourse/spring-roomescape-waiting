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
import roomescape.slot.domain.ReservationSlot;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.util.List;
import java.util.UUID;

import static roomescape.ConcurrentUtils.doConcurrent;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({ReservationService.class})
class ReservationServiceConcurrentTest extends ServiceSupport {

    private final String name = "송송";
    private final String themeName = "테마1";

    private ReservationDate date1;
    private ReservationTime time1;
    private ReservationSlot slot1;
    private Theme theme;

    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        date1 = saveDate(ReservationDateFixture.oneWeekLater());
        time1 = saveTime(ReservationTimeFixture.activeTime15());
        theme = saveTheme(themeName);
        slot1 = saveSlot(ReservationSlot.of(date1, time1, theme));
    }

    @Test
    @DisplayName("동시 예약요청시 하나는 예약, 나머지는 대기로 들어간다.")
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reserve_concurrent() throws InterruptedException {
        // given
        doConcurrent(5, () -> reservationService.reserve(UUID.randomUUID().toString(), slot1.getId()));

        // when
        List<Reservation> reservations = reservationRepository.findReservedAndWaitingBySlotId(slot1.getId());

        // then
        Assertions.assertThat(reservations)
                .hasSize(5);

        Assertions.assertThat(reservations)
                .filteredOn(r -> r.getStatus() == RESERVED)
                .hasSize(1);

        Assertions.assertThat(reservations)
                .filteredOn(r -> r.getStatus() == ReservationStatus.WAITING)
                .hasSize(4);
    }

    @Test
    @DisplayName("내가 동시 예약요청시 하나는 예약, 나머지는 실패가 된다.")
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reserve_concurrent_myself() throws InterruptedException {
        // given
        doConcurrent(2, () -> reservationService.reserve(name, slot1.getId()));

        // when
        List<Reservation> reservations = reservationRepository.findReservedAndWaitingBySlotId(slot1.getId());

        // then
        Assertions.assertThat(reservations)
                .hasSize(1);

        Assertions.assertThat(reservations.getFirst().getName())
                .isEqualTo(name);

        Assertions.assertThat(reservations.getFirst().getStatus())
                .isEqualTo(RESERVED);
    }

    @Test
    @DisplayName("예약자와 대기 1순위가 동시에 취소하면 모두 취소된다.")
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void cancel_sametime_reserved_and_first_waiting() {
        // given
        Reservation reserved = reservationService.reserve("송송", slot1.getId());
        Reservation waiting = reservationService.reserve("대기자", slot1.getId());

        doConcurrent(
                () -> reservationService.cancel(slot1.getId(), reserved.getId(), reserved.getName()),
                () -> reservationService.cancel(slot1.getId(), waiting.getId(), waiting.getName())
        );

        // when
        Reservation reservedCanceled = reservationRepository.findById(reserved.getId()).get();
        Reservation waitingCanceled = reservationRepository.findById(waiting.getId()).get();

        // then
        Assertions.assertThat(reservedCanceled.getStatus())
                .isEqualTo(CANCELED);

        Assertions.assertThat(waitingCanceled.getStatus())
                .isEqualTo(CANCELED);
    }

}
