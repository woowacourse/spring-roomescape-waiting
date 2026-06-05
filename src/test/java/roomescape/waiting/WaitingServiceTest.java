package roomescape.waiting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.application.ReservationPromotionService;
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationPromotionService reservationPromotionService;

    @InjectMocks
    private WaitingService waitingService;

    @Test
    @DisplayName("예약 대기를 저장할 수 있다.")
    void save_테스트_1() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L, null);
        long scheduleId = 1L;
        Reservation reservation = new Reservation(1L, 1L, 1L);
        Waiting savedWaiting = new Waiting(10L, MEMBER_ID, scheduleId);

        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(waitingRepository.existsByScheduleIdAndMemberId(scheduleId, MEMBER_ID))
                .thenReturn(false);
        when(reservationRepository.existsByMemberIdAndScheduleId(MEMBER_ID, scheduleId))
                .thenReturn(false);
        when(reservationRepository.findByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.save(any(Waiting.class)))
                .thenReturn(savedWaiting);
        when(waitingRepository.countByScheduleIdAndIdLessThanEqual(scheduleId, savedWaiting.getId()))
                .thenReturn(3L);

        WaitingResponse response = waitingService.save(request, MEMBER_ID);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.scheduleId()).isEqualTo(scheduleId);
        assertThat(response.waitingOrder()).isEqualTo(3L);
        verify(waitingRepository).save(any(Waiting.class));
    }

    @Test
    @DisplayName("똑같은 사람이 같은 스케줄에 대한 중복 대기를 하면 예외가 발생한다.")
    void save_테스트_2() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L, null);
        long scheduleId = 1L;

        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(waitingRepository.existsByScheduleIdAndMemberId(scheduleId, MEMBER_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(scheduleService).findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        verify(waitingRepository, never()).save(any(Waiting.class));
    }

    @Test
    @DisplayName("유저가 이미 예약한 스케줄이면 다시 대기를 신청할 수 없다.")
    void save_테스트_3() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L, null);
        long scheduleId = 1L;

        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.existsByMemberIdAndScheduleId(MEMBER_ID, scheduleId))
                .thenReturn(true);

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(waitingRepository, never()).save(any(Waiting.class));
    }

    @Test
    @DisplayName("해당 스케줄에 예약/대기가 모두 없으면 대기를 신청할 수 없다.")
    void save_테스트_4() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 4L, 4L, null);
        long scheduleId = 4L;

        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.existsByMemberIdAndScheduleId(MEMBER_ID, scheduleId))
                .thenReturn(false);
        when(waitingRepository.existsByScheduleIdAndMemberId(scheduleId, MEMBER_ID))
                .thenReturn(false);
        when(reservationRepository.findByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.empty());
        when(waitingRepository.existsByScheduleId(scheduleId))
                .thenReturn(false);

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(waitingRepository, never()).save(any(Waiting.class));
    }

    @Test
    @DisplayName("reservationId가 있으면 예약 존재/권한을 검증하고 삭제한 뒤 대기를 저장한다. 즉, 현재 예약을 대기 가능한 다른 스케줄로 변경하려 할 때")
    void save_테스트_5() {
        long reservationIdToCancel = 10L;
        long scheduleId = 1L;
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L, reservationIdToCancel);
        Waiting savedWaiting = new Waiting(11L, MEMBER_ID, scheduleId);
        Reservation reservation = new Reservation(
                10L,
                MEMBER_ID,
                scheduleId
        );

        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.findByIdForPromotion(reservationIdToCancel))
                .thenReturn(Optional.of(reservation));
        when(reservationRepository.existsByMemberIdAndScheduleId(MEMBER_ID, scheduleId))
                .thenReturn(false);
        when(waitingRepository.existsByScheduleIdAndMemberId(scheduleId, MEMBER_ID))
                .thenReturn(false);
        when(reservationRepository.findByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.save(any(Waiting.class)))
                .thenReturn(savedWaiting);
        when(waitingRepository.countByScheduleIdAndIdLessThanEqual(scheduleId, savedWaiting.getId()))
                .thenReturn(1L);

        WaitingResponse response = waitingService.save(request, MEMBER_ID);

        assertThat(response.id()).isEqualTo(11L);
        verify(reservationPromotionService).cancelReservationAndPromoteFirstWaiting(
                reservationIdToCancel,
                reservation.getScheduleId()
        );
    }

    @Test
    @DisplayName("확정예약을 다른 스케줄의 대기로 변경할 경우 취소할 예약이 본인 소유가 아니면 예외가 발생한다.")
    void save_테스트_6() {
        long scheduleId = 1L;
        long reservationIdToCancel = 10L;
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L, reservationIdToCancel);
        Reservation reservation = new Reservation(
                10L,
                999L,
                scheduleId
        );

        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.existsByMemberIdAndScheduleId(MEMBER_ID, scheduleId))
                .thenReturn(false);
        when(waitingRepository.existsByScheduleIdAndMemberId(scheduleId, MEMBER_ID))
                .thenReturn(false);
        when(reservationRepository.findByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.of(reservation));
        when(reservationRepository.findByIdForPromotion(reservationIdToCancel))
                .thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> waitingService.save(request, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(waitingRepository, never()).save(any(Waiting.class));
        verify(reservationRepository, never()).deleteById(anyLong());
        verify(reservationPromotionService, never()).cancelReservationAndPromoteFirstWaiting(anyLong(), anyLong());
    }

    @Test
    @DisplayName("reservationId가 없으면 현재 예약을 삭제할 수 있는 지 확인하지 않고 대기를 저장한다. 즉, 현재 예약이 없는 사람이 대기를 하려 할 때")
    void save_테스트_7() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L, null);
        long scheduleId = 1L;
        Reservation reservation = new Reservation(1L, MEMBER_ID, scheduleId);
        Waiting waiting = new Waiting(
                10L,
                MEMBER_ID,
                scheduleId
        );
        long waitingOrder = 2L;
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.existsByMemberIdAndScheduleId(MEMBER_ID, scheduleId))
                .thenReturn(false);
        when(waitingRepository.existsByScheduleIdAndMemberId(scheduleId, MEMBER_ID))
                .thenReturn(false);
        when(reservationRepository.findByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.save(any(Waiting.class)))
                .thenReturn(waiting);
        when(waitingRepository.countByScheduleIdAndIdLessThanEqual(scheduleId, waiting.getId()))
                .thenReturn(waitingOrder);

        WaitingResponse response = waitingService.save(request, MEMBER_ID);

        assertThat(response.id()).isEqualTo(waiting.getId());
        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.scheduleId()).isEqualTo(scheduleId);
        assertThat(response.waitingOrder()).isEqualTo(waitingOrder);

        verify(reservationRepository, never()).findById(anyLong());
        verify(reservationRepository, never()).deleteById(anyLong());
        verify(reservationPromotionService, never()).cancelReservationAndPromoteFirstWaiting(anyLong(), anyLong());
        verify(waitingRepository).save(any(Waiting.class));
    }

    @Test
    @DisplayName("본인의 예약 대기를 취소할 수 있다.")
    void cancelByIdForUser_테스트_1() {
        Waiting waiting = new Waiting(1L, 1L, 1L);
        when(waitingRepository.findByIdForPromotion(waiting.getId())).thenReturn(Optional.of(waiting));

        assertThatCode(() -> waitingService.cancelByIdForUser(1L, 1L))
                .doesNotThrowAnyException();

        verify(waitingRepository).deleteById(1L);
    }

    @Test
    @DisplayName("본인의 예약 대기가 아닌데 취소를 시도하면 예외가 발생한다.")
    void cancelByIdForUser_테스트_2() {
        Waiting waiting = new Waiting(1L, 1L, 1L);
        when(waitingRepository.findByIdForPromotion(waiting.getId())).thenReturn(Optional.of(waiting));

        assertThatThrownBy(() -> waitingService.cancelByIdForUser(1L, 2L))
                .isInstanceOf(EscapeRoomException.class);

        verify(waitingRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("없는 예약 대기를 취소할 경우 성공처리 한다.")
    void cancelByIdForUser_테스트_3() {
        when(waitingRepository.findByIdForPromotion(999L)).thenReturn(Optional.empty());

        assertThatCode(() -> waitingService.cancelByIdForUser(999L, 1L))
                .doesNotThrowAnyException();

        verify(waitingRepository, never()).deleteById(999L);
    }
}
