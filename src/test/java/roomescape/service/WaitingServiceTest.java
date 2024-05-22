package roomescape.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Waiting;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.DATE_MAY_TWENTY;
import static roomescape.TestFixture.RESERVATION_TIME_ONE;
import static roomescape.TestFixture.THEME_COMIC;

@SpringBootTest
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("본인의 예약이 아니라면 예외가 발생한다.")
    void cancel() {
        assertThatThrownBy(() -> waitingService.cancel(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("중복 대기의 경우 예외가 발생한다.")
    void create() {
        //given
        Waiting waiting = new Waiting(ADMIN(1L), DATE_MAY_TWENTY, RESERVATION_TIME_ONE(1L), THEME_COMIC(1L));

        //when & then
        assertThatThrownBy(() -> waitingService.create(waiting))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약이 없는 경우 대기를 예약으로 저장한다.")
    void createWithoutReservation() {
        // given
        LocalDate now = LocalDate.now().plusDays(1);
        Waiting waiting = new Waiting(ADMIN(1L), now, RESERVATION_TIME_ONE(1L), THEME_COMIC(1L));

        // when
        ReservationResponse reservationResponse = waitingService.create(waiting);

        // then
        Assertions.assertAll(
                () -> assertThat(waitingRepository.findById(reservationResponse.id())).isEmpty(),
                () -> assertThat(reservationRepository.findById(reservationResponse.id())).isPresent()
        );
    }
}
