package roomescape.service;

import static org.mockito.BDDMockito.given;

import common.exception.RoomEscapeException;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 정상적인_시간_삭제는_성공해야_한다() {
        given(reservationTimeRepository.existsById(1L)).willReturn(true);
        given(reservationRepository.existsByTimeId(1L)).willReturn(false);

        Assertions.assertThatNoException().isThrownBy(() -> reservationTimeService.delete(1L));
    }

    @Test
    void 존재하지_않는_시간_삭제시_예외가_발생한다() {
        given(reservationTimeRepository.existsById(999L)).willReturn(false);

        Assertions.assertThatThrownBy(() -> reservationTimeService.delete(999L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약이_있는_시간_삭제시_예외가_발생한다() {
        given(reservationTimeRepository.existsById(1L)).willReturn(true);
        given(reservationRepository.existsByTimeId(1L)).willReturn(true);

        Assertions.assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 과거_예약_가능_시간_조회시_예외가_발생한다() {
        AvailableTimeFindRequest request = new AvailableTimeFindRequest(LocalDate.MIN, 1L);

        Assertions.assertThatThrownBy(() -> reservationTimeService.findAvailable(request, LocalDate.MAX))
                .isInstanceOf(RoomEscapeException.class);
    }
}
