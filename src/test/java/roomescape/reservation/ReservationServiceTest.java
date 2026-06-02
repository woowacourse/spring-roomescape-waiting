package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.MyReservationsAndWaitingsDetailResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationDetailProjection reservationDetail(
            Long reservationId,
            Long memberId,
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalTime startAt
    ) {
        return new ReservationDetailProjection(
                reservationId,
                memberId,
                "member",
                date,
                themeId,
                "theme",
                "description",
                "thumbnail",
                timeId,
                startAt
        );
    }

    @Test
    @DisplayName("유저는 본인 예약 삭제에 성공한다.")
    void deleteByIdForUser_테스트_1() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(1L);
        when(waitingRepository.findFirstByScheduleId(1L)).thenReturn(Optional.empty());

        assertThatCode(() -> reservationService.deleteByIdForUser(reservationId, 1L))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("유저는 타인 예약 삭제를 할 수 없다.")
    void deleteByIdForUser_테스트_2() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 2L, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.deleteByIdForUser(reservationId, 1L))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("유저 예약 삭제 시 해당 슬롯의 선두 대기자는 자동 승격된다.")
    void deleteByIdForUser_테스트_3() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        long scheduleId = 10L;
        Waiting waiting = new Waiting(100L, 3L, scheduleId);

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(scheduleId);
        when(waitingRepository.findFirstByScheduleId(scheduleId)).thenReturn(Optional.of(waiting));

        reservationService.deleteByIdForUser(reservationId, 1L);

        verify(reservationRepository).deleteById(reservationId);
        verify(reservationRepository).save(argThat(promoted ->
                promoted.getMemberId().equals(waiting.getMemberId())
                        && promoted.getScheduleId().equals(waiting.getScheduleId())
        ));
        verify(waitingRepository).deleteById(waiting.getId());
    }


    @Test
    @DisplayName("매니저는 예약 삭제에 성공한다.")
    void deleteByIdForManager_테스트_1() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(1L);
        when(waitingRepository.findFirstByScheduleId(1L)).thenReturn(Optional.empty());

        assertThatCode(() -> reservationService.deleteByIdForManager(reservationId))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("유저는 본인 예약 수정에 성공한다.")
    void updateForUser_테스트_1() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );
        Reservation updated = new Reservation(reservationId, 1L, 99L);

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(3L);
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), 3L))
                .thenReturn(99L);
        when(reservationRepository.existsByScheduleIdAndIdNot(99L, reservationId)).thenReturn(false);
        when(reservationRepository.updateScheduleById(reservationId, 99L)).thenReturn(1);
        when(waitingRepository.findFirstByScheduleId(3L)).thenReturn(Optional.empty());
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        ReservationSaveResponse response = reservationService.updateForUser(request, reservationId, 1L);

        assertThat(response.id()).isEqualTo(reservationId);
        verify(reservationRepository).updateScheduleById(reservationId, 99L);
    }

    @Test
    @DisplayName("매니저는 예약 수정에 성공한다.")
    void updateForManager_테스트_1() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );
        Reservation updated = new Reservation(reservationId, 1L, 99L);

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(3L);
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), 3L))
                .thenReturn(99L);
        when(reservationRepository.existsByScheduleIdAndIdNot(99L, reservationId)).thenReturn(false);
        when(reservationRepository.updateScheduleById(reservationId, 99L)).thenReturn(1);
        when(waitingRepository.findFirstByScheduleId(3L)).thenReturn(Optional.empty());
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        ReservationSaveResponse response = reservationService.updateForManager(request, reservationId);

        assertThat(response.id()).isEqualTo(reservationId);
        verify(reservationRepository).updateScheduleById(reservationId, 99L);
    }

    @Test
    @DisplayName("유저는 타인 예약 수정을 할 수 없다.")
    void updateForUser_테스트_2() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 2L, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, 1L))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateScheduleById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("예약 시간이 과거면 수정에 실패한다.")
    void updateForUser_테스트_3() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        doNothing().when(scheduleService).validateNotPastDate(oldReservation.date());
        doThrow(IllegalStateException.class).when(scheduleService).validateNotPastTime(oldReservation.date(), oldReservation.getTime());

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, 1L))
                .isInstanceOf(IllegalStateException.class);
        verify(reservationRepository, never()).updateScheduleById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("유저 예약 수정으로 기존 슬롯이 비면 선두 대기자가 자동 승격된다.")
    void updateForUser_테스트_4() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );
        long oldScheduleId = 30L;
        long newScheduleId = 99L;
        Waiting waiting = new Waiting(200L, 5L, oldScheduleId);
        Reservation updated = new Reservation(reservationId, 1L, newScheduleId);

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(oldScheduleId);
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), 3L))
                .thenReturn(newScheduleId);
        when(reservationRepository.existsByScheduleIdAndIdNot(newScheduleId, reservationId)).thenReturn(false);
        when(reservationRepository.updateScheduleById(reservationId, newScheduleId)).thenReturn(1);
        when(waitingRepository.findFirstByScheduleId(oldScheduleId)).thenReturn(Optional.of(waiting));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        reservationService.updateForUser(request, reservationId, 1L);

        verify(reservationRepository).updateScheduleById(reservationId, newScheduleId);
        verify(reservationRepository).save(argThat(promoted ->
                promoted.getMemberId().equals(waiting.getMemberId())
                        && promoted.getScheduleId().equals(waiting.getScheduleId())
        ));
        verify(waitingRepository).deleteById(waiting.getId());
    }

    @Test
    @DisplayName("예약 수정 시 기존 스케줄과 새 스케줄이 같으면 예외가 발생한다")
    void updateForUser_테스트_5() {
        long reservationId = 1L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, 1L, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );
        long oldScheduleId = 30L;
        long newScheduleId = 30L;

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(
                oldReservation.date(),
                oldReservation.getTimeId(),
                oldReservation.getThemeId()
        )).thenReturn(oldScheduleId);
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), 3L))
                .thenReturn(newScheduleId);

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, 1L))
                .isInstanceOf(EscapeRoomException.class);

        verify(reservationRepository, never()).updateScheduleById(reservationId, newScheduleId);
    }

    @Test
    @DisplayName("UPCOMING 기간으로 내 예약/대기 조회 시 UPCOMING 조회 메서드를 호출한다.")
    void findMyReservationsAndWaitingsByPeriod_테스트_1() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 10, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation = reservationDetail(
                1L, 1L, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(10, 0)
        );
        WaitingDetailProjection waiting = new WaitingDetailProjection(
                10L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(11, 0), 1L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(1L, now))
                .thenReturn(java.util.List.of(reservation));
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(1L, now))
                .thenReturn(java.util.List.of(waiting));

        List<MyReservationsAndWaitingsDetailResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(1L, ReservationPeriod.UPCOMING);

        assertThat(result).hasSize(2);
        verify(reservationRepository).findUpcomingReservationDetailsByMemberId(1L, now);
        verify(waitingRepository).findUpcomingWaitingDetailsByMemberId(1L, now);
        verify(reservationRepository, never()).findPastReservationDetailsByMemberId(anyLong(), any());
        verify(waitingRepository, never()).findPastWaitingDetailsByMemberId(anyLong(), any());
    }

    @Test
    @DisplayName("HISTORY 기간으로 내 예약/대기 조회 시 HISTORY 조회 메서드를 호출한다.")
    void findMyReservationsAndWaitingsByPeriod_테스트_2() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 10, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(1L, now))
                .thenReturn(java.util.List.of());
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(1L, now))
                .thenReturn(java.util.List.of());

        ReservationDetailProjection reservation = reservationDetail(
                2L, 1L, LocalDate.of(2026, 5, 4), 1L, 1L, LocalTime.of(10, 0)
        );
        WaitingDetailProjection waiting = new WaitingDetailProjection(
                20L, "member", LocalDate.of(2026, 5, 4), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(9, 0), 2L
        );

        when(reservationRepository.findPastReservationDetailsByMemberId(1L, now))
                .thenReturn(java.util.List.of(reservation));
        when(waitingRepository.findPastWaitingDetailsByMemberId(1L, now))
                .thenReturn(java.util.List.of(waiting));

        List<MyReservationsAndWaitingsDetailResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(1L, ReservationPeriod.HISTORY);

        assertThat(result).hasSize(2);
        verify(reservationRepository).findPastReservationDetailsByMemberId(1L, now);
        verify(waitingRepository).findPastWaitingDetailsByMemberId(1L, now);
    }

    @Test
    @DisplayName("동일한 날짜와 시간에서는 예약(RESERVED)이 대기(WAITING)보다 먼저 조회된다.")
    void findMyReservationsAndWaitingsByPeriod_테스트_3() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 9, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation = reservationDetail(
                100L, 1L, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(10, 0)
        );
        WaitingDetailProjection waiting = new WaitingDetailProjection(
                200L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(10, 0), 1L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(1L, now))
                .thenReturn(List.of(reservation));
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(1L, now))
                .thenReturn(List.of(waiting));

        List<MyReservationsAndWaitingsDetailResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(1L, ReservationPeriod.UPCOMING);

        assertThat(result).extracting(MyReservationsAndWaitingsDetailResponse::status)
                .containsExactly(ReservationStatus.RESERVED, ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("UPCOMING 조회는 날짜/시간 오름차순이며, 동률이면 예약을 우선한다.")
    void findMyReservationsAndWaitingsByPeriod_테스트_4() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 9, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation1 = reservationDetail(
                101L, 1L, LocalDate.of(2026, 5, 6), 1L, 1L, LocalTime.of(10, 0)
        );
        ReservationDetailProjection reservation2 = reservationDetail(
                102L, 1L, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(12, 0)
        );
        WaitingDetailProjection waiting1 = new WaitingDetailProjection(
                201L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(10, 0), 1L
        );
        WaitingDetailProjection waiting2 = new WaitingDetailProjection(
                202L, "member", LocalDate.of(2026, 5, 6), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(9, 0), 2L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(1L, now))
                .thenReturn(List.of(reservation1, reservation2));
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(1L, now))
                .thenReturn(List.of(waiting1, waiting2));

        List<MyReservationsAndWaitingsDetailResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(1L, ReservationPeriod.UPCOMING);

        assertThat(result).extracting(MyReservationsAndWaitingsDetailResponse::id)
                .containsExactly(201L, 102L, 202L, 101L);
    }

    @Test
    @DisplayName("HISTORY 조회는 날짜/시간 내림차순이며, 동률이면 예약을 우선한다.")
    void findMyReservationsAndWaitingsByPeriod_테스트_5() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 7, 10, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation1 = reservationDetail(
                301L, 1L, LocalDate.of(2026, 5, 6), 1L, 1L, LocalTime.of(10, 0)
        );
        ReservationDetailProjection reservation2 = reservationDetail(
                302L, 1L, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(12, 0)
        );
        WaitingDetailProjection waiting1 = new WaitingDetailProjection(
                401L, "member", LocalDate.of(2026, 5, 6), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(12, 0), 1L
        );
        WaitingDetailProjection waiting2 = new WaitingDetailProjection(
                402L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(9, 0), 2L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(1L, now))
                .thenReturn(List.of());
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(1L, now))
                .thenReturn(List.of());
        when(reservationRepository.findPastReservationDetailsByMemberId(1L, now))
                .thenReturn(List.of(reservation1, reservation2));
        when(waitingRepository.findPastWaitingDetailsByMemberId(1L, now))
                .thenReturn(List.of(waiting1, waiting2));

        List<MyReservationsAndWaitingsDetailResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(1L, ReservationPeriod.HISTORY);

        assertThat(result).extracting(MyReservationsAndWaitingsDetailResponse::id)
                .containsExactly(401L, 301L, 302L, 402L);
    }
}
