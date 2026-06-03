package roomescape.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationRepository;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceMockTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
    private static final LocalDate FUTURE = LocalDate.of(2999, 1, 1);

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
        Reservation reservation = new Reservation(1L, "티뉴", FUTURE, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.findMyReservation(1L, "민욱"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void findMyReservation은_본인_예약이면_예약을_반환한다() {
        Reservation reservation = new Reservation(1L, "민욱", FUTURE, TIME, THEME);
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
