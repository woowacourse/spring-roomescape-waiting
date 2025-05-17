package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.time.controller.request.ReservationTimeCreateRequest;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.ReservationTimeService;

@ExtendWith(MockitoExtension.class)
public class ReservationTimeServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 예약시간을_삭제한다() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        when(reservationRepository.existsByReservationTimeId(1L)).thenReturn(false);
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        reservationTimeService.deleteById(1L);
        assertThat(reservationTimeService.getAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_예약시간을_삭제할_수_없다() {
        when(reservationRepository.existsByReservationTimeId(3L)).thenReturn(false);
        when(reservationTimeRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservationTimeService.deleteById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 이미_해당_시간의_예약이_존재한다면_삭제할_수_없다() {
        when(reservationRepository.existsByReservationTimeId(1L)).thenReturn(true);
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_존재하는_시간은_추가할_수_없다() {
        when(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(true);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(
                LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
