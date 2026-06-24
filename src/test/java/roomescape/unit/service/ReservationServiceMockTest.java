package roomescape.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.fixture.ReservationFixture;
import roomescape.repository.ReservationRepository;
import roomescape.service.ReservationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceMockTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void findMyReservation은_예약이_없으면_NotFoundException을_던진다() {
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findMyReservation(1L, "민욱"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findMyReservation은_본인_예약이_아니면_UnauthorizedException을_던진다() {
        Reservation reservation = ReservationFixture.builder().id(1L).name("티뉴").build();
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.findMyReservation(1L, "민욱"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void findMyReservation은_본인_예약이면_예약을_반환한다() {
        Reservation reservation = ReservationFixture.builder().id(1L).name("민욱").build();
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThat(reservationService.findMyReservation(1L, "민욱")).isEqualTo(reservation);
    }

    @Test
    void getReservationPage는_page와_size로_offset을_계산해_조회한다() {
        given(reservationRepository.findAll(10, 5)).willReturn(List.of());
        given(reservationRepository.count()).willReturn(0L);

        reservationService.getReservationPage(2, 5);

        verify(reservationRepository).findAll(10, 5);
    }
}
