package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.repository.ReservationRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    @DisplayName("예약 대기를 예약으로 전환한다.")
    @Test
    void bookPendingReservation() {
        //given
        reservationRepository.deleteById(1L);

        //when
        reservationService.bookPendingReservation(2L);
        Reservation result = reservationRepository.findById(2L).orElseThrow();

        //then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.BOOKING);
    }

    @DisplayName("예약 대기를 예약으로 전환시 BOOKING이 존재하면 예외가 발생한다.")
    @Test
    void bookingPendingReservationWhenAlreadyHasBooking() {
        //when
        assertThatThrownBy(() -> reservationService.bookPendingReservation(13L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
