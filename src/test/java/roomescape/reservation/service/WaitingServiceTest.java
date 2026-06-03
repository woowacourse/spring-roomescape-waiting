package roomescape.reservation.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    @InjectMocks
    private WaitingService waitingService;

    @DisplayName("대기 취소를 할 수 있어야 한다.")
    @Test
    void cancel_user_waiting() {
        Long id = 1L;
        WaitingDetail waitingDetail = new WaitingDetail(id, "타스", LocalDate.of(2026, 5, 27), 1L, "테마", "설명", "url", 1L, LocalTime.of(10, 0));

        when(waitingRepository.findDetailById(id)).thenReturn(Optional.of(waitingDetail));
        when(waitingRepository.delete(id)).thenReturn(1);

        waitingService.delete(id, "타스");

        verify(waitingRepository).delete(id);
    }

    @DisplayName("취소하려는 대기 ID의 예약자 명이 다르다면 예외를 발생시켜야 한다.")
    @Test
    void exception_when_name_is_unmatched() {
        Long id = 1L;
        WaitingDetail waitingDetail = new WaitingDetail(id, "타스", LocalDate.of(2026, 5, 27), 1L, "테마", "설명", "url", 1L, LocalTime.of(10, 0));

        when(waitingRepository.findDetailById(id)).thenReturn(Optional.of(waitingDetail));

        assertThatThrownBy(() -> waitingService.delete(id, "카야"))
                .isExactlyInstanceOf(RoomEscapeException.class)
                .hasMessage(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS.message());
    }
}
