package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.CancelService;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.infra.event.ReservationCancelEvent;

@SpringBootTest
@RecordApplicationEvents
class ReservationCancelEventTest {

    @Autowired
    private ApplicationEvents events;
    @Autowired
    private CancelService cancelService;
    @Autowired
    private ReservationRepository reservationRepository;

    @Disabled
    @DisplayName("예약 취소 이벤트 발생")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/times.sql", "/test-data/themes.sql",
            "/test-data/reservations-details.sql", "/test-data/reservations.sql"})
    void cancelReservationEventTest() {
        // given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        // when
        cancelService.forceCancelReservation(reservation.getId());

        // then
        assertThat(events.stream(ReservationCancelEvent.class))
                .hasSize(1);
    }
}
