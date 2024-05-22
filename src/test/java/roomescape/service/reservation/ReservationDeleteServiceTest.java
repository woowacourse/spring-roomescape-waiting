package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationDeleteServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDeleteService reservationDeleteService;


    @Test
    @DisplayName("예약 취소가 발생할 시 예약 대기가 승인된다.")
    void deleteReservedTime_changeWaitingToReserved() {
        Reservation beforeUpdatedReservation = reservationRepository.findById(2L).get();
        assertThat(beforeUpdatedReservation.isReserved()).isFalse();

        reservationDeleteService.deleteReservation(1L);
        Reservation updatedReservation = reservationRepository.findById(2L).get();
        assertThat(updatedReservation.isReserved()).isTrue();
    }
}
