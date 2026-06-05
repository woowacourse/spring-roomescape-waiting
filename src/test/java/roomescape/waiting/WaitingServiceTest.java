package roomescape.waiting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.slot.application.SlotAssembler;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingResponse;
import roomescape.waiting.application.port.out.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private SlotAssembler slotAssembler;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingService waitingService;

    @Test
    @DisplayName("예약 대기를 저장할 수 있다.")
    void save_테스트_1() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        long slotId = 1L;
        Waiting firstWaiting = Waiting.of(8L, 3L, slotId);
        Waiting secondWaiting = Waiting.of(9L, 2L, slotId);
        Waiting savedWaiting = Waiting.of(10L, MEMBER_ID, slotId);

        when(slotAssembler.assembleExisting(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot(slotId, request));
        when(waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, slotId))
                .thenReturn(false);
        when(reservationRepository.existsByMemberIdAndSlotId(MEMBER_ID, slotId))
                .thenReturn(false);
        when(reservationRepository.existsBySlotId(slotId))
                .thenReturn(false);
        when(waitingRepository.existsBySlotId(slotId))
                .thenReturn(true);
        when(waitingRepository.save(any(Waiting.class)))
                .thenReturn(savedWaiting);
        when(waitingRepository.findAllBySlotIdOrderById(slotId))
                .thenReturn(List.of(firstWaiting, secondWaiting, savedWaiting));

        WaitingResponse response = waitingService.save(request, MEMBER_ID);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.slotId()).isEqualTo(slotId);
        assertThat(response.waitingOrder()).isEqualTo(3L);
        verify(waitingRepository).save(any(Waiting.class));
    }

    @Test
    @DisplayName("똑같은 사람이 같은 슬롯에 대한 중복 대기를 하면 예외가 발생한다.")
    void save_테스트_2() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        long slotId = 1L;

        when(slotAssembler.assembleExisting(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot(slotId, request));
        when(waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, slotId))
                .thenReturn(true);

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOfSatisfying(EscapeRoomException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WAITING_ALREADY_EXIST)
                );

        verify(slotAssembler).assembleExisting(request.date(), request.timeId(), request.themeId());
        verify(waitingRepository, never()).save(any(Waiting.class));
    }

    @Test
    @DisplayName("유저가 이미 예약한 슬롯이면 다시 대기를 신청할 수 없다.")
    void save_테스트_3() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        long slotId = 1L;

        when(slotAssembler.assembleExisting(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot(slotId, request));
        when(reservationRepository.existsByMemberIdAndSlotId(MEMBER_ID, slotId))
                .thenReturn(true);

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOfSatisfying(EscapeRoomException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION)
                );

        verify(waitingRepository, never()).save(any(Waiting.class));
    }

    @Test
    @DisplayName("해당 슬롯에 예약/대기가 모두 없으면 대기를 신청할 수 없다.")
    void save_테스트_4() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 4L, 4L);
        long slotId = 4L;

        when(slotAssembler.assembleExisting(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot(slotId, request));
        when(reservationRepository.existsByMemberIdAndSlotId(MEMBER_ID, slotId))
                .thenReturn(false);
        when(waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, slotId))
                .thenReturn(false);
        when(reservationRepository.existsBySlotId(slotId))
                .thenReturn(false);
        when(waitingRepository.existsBySlotId(slotId))
                .thenReturn(false);

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(waitingRepository, never()).save(any(Waiting.class));
    }

    @Test
    @DisplayName("예약된 슬롯에는 첫 번째 대기를 신청할 수 있다.")
    void save_테스트_5() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        long slotId = 1L;
        Waiting savedWaiting = Waiting.of(10L, MEMBER_ID, slotId);

        when(slotAssembler.assembleExisting(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot(slotId, request));
        when(reservationRepository.existsByMemberIdAndSlotId(MEMBER_ID, slotId))
                .thenReturn(false);
        when(waitingRepository.existsBySlotIdAndMemberId(MEMBER_ID, slotId))
                .thenReturn(false);
        when(reservationRepository.existsBySlotId(slotId))
                .thenReturn(true);
        when(waitingRepository.save(any(Waiting.class)))
                .thenReturn(savedWaiting);
        when(waitingRepository.findAllBySlotIdOrderById(slotId))
                .thenReturn(List.of(savedWaiting));

        WaitingResponse response = waitingService.save(request, MEMBER_ID);

        assertThat(response.id()).isEqualTo(savedWaiting.getId());
        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.slotId()).isEqualTo(slotId);
        assertThat(response.waitingOrder()).isEqualTo(1L);

        verify(waitingRepository).save(any(Waiting.class));
    }

    @Test
    @DisplayName("본인의 예약 대기를 취소할 수 있다.")
    void deleteByIdForUser_테스트_1() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);
        when(waitingRepository.findById(waiting.getId())).thenReturn(Optional.of(waiting));

        assertThatCode(() -> waitingService.deleteByIdForUser(1L, 1L))
                .doesNotThrowAnyException();

        verify(waitingRepository).deleteById(1L);
    }

    @Test
    @DisplayName("본인의 예약 대기가 아닌데 취소를 시도하면 예외가 발생한다.")
    void deleteByIdForUser_테스트_2() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);
        when(waitingRepository.findById(waiting.getId())).thenReturn(Optional.of(waiting));

        assertThatThrownBy(() -> waitingService.deleteByIdForUser(1L, 2L))
                .isInstanceOf(EscapeRoomException.class);

        verify(waitingRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("없는 예약 대기를 취소할 경우 성공처리 한다.")
    void deleteByIdForUser_테스트_3() {
        when(waitingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatCode(() -> waitingService.deleteByIdForUser(999L, 1L))
                .doesNotThrowAnyException();

        verify(waitingRepository, never()).deleteById(999L);
    }

    private Slot slot(long slotId, WaitingRequest request) {
        return Slot.of(
                slotId,
                request.date(),
                new ReservationTime(request.timeId(), LocalTime.of(10, 0)),
                new Theme(request.themeId(), "theme", "description", "thumbnail")
        );
    }
}
