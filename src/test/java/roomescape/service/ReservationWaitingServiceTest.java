package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.service.dto.ReservationWaitingRequest;
import roomescape.service.dto.ReservationWaitingResponse;


@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Test
    void addReservationWaitingTest() {
        //given
        final long memberId = 1L;
        final long timeId = 1L;
        final long themeId = 1L;
        final LocalDate localDate = LocalDate.of(2025, 5, 30);
        final ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest(
                localDate, themeId, timeId);

        //when
        final ReservationWaitingResponse expected = reservationWaitingService.addReservationWaiting(
                reservationWaitingRequest, memberId);

        //then
        assertAll(
                () -> assertThat(expected.time().id()).isEqualTo(timeId),
                () -> assertThat(expected.theme().id()).isEqualTo(themeId),
                () -> assertThat(expected.date()).isEqualTo(localDate)
        );
    }

    @Test
    void removeReservationWaitingTest() {
        //given
        final long memberId = 1L;
        final long timeId = 1L;
        final long themeId = 1L;
        final LocalDate localDate = LocalDate.of(2025, 5, 30);
        final ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest(
                localDate, themeId, timeId);
        final ReservationWaitingResponse saved = reservationWaitingService.addReservationWaiting(
                reservationWaitingRequest, memberId);

        //should
        assertThatCode(() -> reservationWaitingService.removeReservationWaiting(saved.id())).doesNotThrowAnyException();
    }
}
