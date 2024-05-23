package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.waiting.service.WaitingService;

@ExtendWith(MockitoExtension.class)
class ReservationFindMineUsecaseTest {
    private final MyReservationResponse RESERVATION = new MyReservationResponse(
            1L, "테마1", LocalDate.of(2024, 5, 5), LocalTime.of(11, 0), "예약", null);
    private final MyReservationResponse WAITING = new MyReservationResponse(
            2L, "테마2", LocalDate.of(2024, 5, 6), LocalTime.of(11, 0), "1번째 예약 대기", 3L);
    
    @Mock
    private ReservationFindService reservationFindService;
    @Mock
    private WaitingService waitingService;
    @InjectMocks
    private ReservationFindMineUsecase reservationFindMineUsecase;
    
    @DisplayName("내 예약 및 예약 대기를 시간 순서로 조회할 수 있다.")
    @Test
    void findMyReservationsTest() {
        given(reservationFindService.findReservationsByMemberId(1L)).willReturn(List.of(RESERVATION));
        given(waitingService.findWaitingsByMemberId(1L)).willReturn(List.of(WAITING));
        List<MyReservationResponse> expected = List.of(RESERVATION, WAITING);

        List<MyReservationResponse> actual = reservationFindMineUsecase.findMyReservations(1L);

        assertThat(actual).isEqualTo(expected);
    }
}
