package roomescape.reservationtime.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservationtime.application.ReservationTimeAssembler;
import roomescape.reservationtime.application.ReservationTimeService;
import roomescape.reservationtime.application.dto.request.ReservationTimeSaveRequest;
import roomescape.reservationtime.application.dto.response.ReservationTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeSaveResponse;
import roomescape.reservationtime.application.port.out.ReservationTimeRepository;
import roomescape.slot.application.SlotUsageValidator;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationTimeServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private SlotUsageValidator slotUsageValidator;

    @Mock
    private ReservationTimeAssembler reservationTimeAssembler;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("매니저는 시간 슬롯을 저장할 수 있다.")
    void manager_saves_reservation_time_successfully() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(14, 0));
        ReservationTime saved = new ReservationTime(5L, LocalTime.of(14, 0));
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(14, 0));

        when(reservationTimeRepository.existsAlreadyTime(request.startAt())).thenReturn(false);
        when(reservationTimeAssembler.assemble(request.startAt())).thenReturn(reservationTime);
        when(reservationTimeRepository.save(reservationTime)).thenReturn(saved);

        ReservationTimeSaveResponse response = reservationTimeService.save(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.startAt()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("매니저는 전체 시간 슬롯을 조회할 수 있다.")
    void manager_finds_all_reservation_times_successfully() {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );
        when(reservationTimeRepository.findAll()).thenReturn(reservationTimes);

        List<ReservationTimeFindResponse> response = reservationTimeService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("슬롯에 시간에 대한 참조가 존재하면 시간 삭제에 실패한다.")
    void reservation_time_referenced_by_slot_cannot_be_deleted() {
        // given
        long timeId = 1L;
        doThrow(new IllegalStateException()).when(slotUsageValidator).validateTimeDeletable(timeId);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOf(IllegalStateException.class);

        verify(reservationTimeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("슬롯에 시간에 대한 참조가 존재하지 않으면 시간 삭제에 성공한다.")
    void unreferenced_reservation_time_is_deleted_successfully() {
        // given
        long timeId = 1L;

        // when
        reservationTimeService.delete(timeId);

        // then
        verify(reservationTimeRepository).deleteById(anyLong());
    }
}
