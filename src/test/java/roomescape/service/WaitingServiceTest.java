package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Waiting;
import roomescape.dto.reservation.ReservationResponse;

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
    @DisplayName("예약 대기를 승인하면 예약이 된다.")
    void approve() {
        // when
        ReservationResponse response = waitingService.approve(1L);

        // then
        assertThat(response.id()).isNotNull();
    }
}
