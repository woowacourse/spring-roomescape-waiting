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
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.infrastructure.WaitingRepository;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;

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
    void deleteById_user_success() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatCode(() -> reservationService.deleteByIdForUser(reservationId, MEMBER_ID))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("매니저는 예약 삭제에 성공한다.")
    void deleteById_manager_success() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatCode(() -> reservationService.deleteById(reservationId))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("유저는 타인 예약 삭제를 할 수 없다.")
    void deleteById_user_other_member_forbidden() {
        long reservationId = 1L;
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, OTHER_MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0)
        );
        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.deleteByIdForUser(reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("유저는 본인 예약 수정에 성공한다.")
    void update_user_success() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );
        Reservation updated = new Reservation(reservationId, MEMBER_ID, 99L);

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), 3L))
                .thenReturn(99L);
        when(reservationRepository.existsByScheduleIdAndIdNot(99L, reservationId)).thenReturn(false);
        when(reservationRepository.updateScheduleById(reservationId, 99L)).thenReturn(1);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        ReservationSaveResponse response = reservationService.updateForUser(request, reservationId, MEMBER_ID);

        assertThat(response.id()).isEqualTo(reservationId);
        verify(reservationRepository).updateScheduleById(reservationId, 99L);
    }

    @Test
    @DisplayName("매니저는 예약 수정에 성공한다.")
    void update_manager_success() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );
        Reservation updated = new Reservation(reservationId, MEMBER_ID, 99L);

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), 3L))
                .thenReturn(99L);
        when(reservationRepository.existsByScheduleIdAndIdNot(99L, reservationId)).thenReturn(false);
        when(reservationRepository.updateScheduleById(reservationId, 99L)).thenReturn(1);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        ReservationSaveResponse response = reservationService.update(request, reservationId);

        assertThat(response.id()).isEqualTo(reservationId);
        verify(reservationRepository).updateScheduleById(reservationId, 99L);
    }

    @Test
    @DisplayName("유저는 타인 예약 수정을 할 수 없다.")
    void update_user_other_member_forbidden() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, OTHER_MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateScheduleById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("예약 시간이 과거면 수정에 실패한다.")
    void update_past_time_fail() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        ReservationDetailProjection oldReservation = reservationDetail(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0)
        );

        when(reservationRepository.findDetailById(reservationId)).thenReturn(Optional.of(oldReservation));
        doNothing().when(scheduleService).validateNotPastDate(oldReservation.date());
        doThrow(IllegalStateException.class).when(scheduleService).validateNotPastTime(oldReservation.date(), oldReservation.getTime());

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, MEMBER_ID))
                .isInstanceOf(IllegalStateException.class);
        verify(reservationRepository, never()).updateScheduleById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("UPCOMING 기간으로 내 예약/대기 조회 시 UPCOMING 조회 메서드를 호출한다.")
    void findMyReservationsAndWaitingsByPeriod_upcoming() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 10, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation = reservationDetail(
                1L, MEMBER_ID, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(10, 0)
        );
        WaitingDetailProjection waiting = new WaitingDetailProjection(
                10L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(11, 0), 1L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(java.util.List.of(reservation));
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(java.util.List.of(waiting));

        List<ReservationDetailFindResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(MEMBER_ID, ReservationPeriod.UPCOMING);

        assertThat(result).hasSize(2);
        verify(reservationRepository).findUpcomingReservationDetailsByMemberId(MEMBER_ID, now);
        verify(waitingRepository).findUpcomingWaitingDetailsByMemberId(MEMBER_ID, now);
        verify(reservationRepository, never()).findPastReservationDetailsByMemberId(anyLong(), any());
        verify(waitingRepository, never()).findPastWaitingDetailsByMemberId(anyLong(), any());
    }

    @Test
    @DisplayName("HISTORY 기간으로 내 예약/대기 조회 시 HISTORY 조회 메서드를 호출한다.")
    void findMyReservationsAndWaitingsByPeriod_history() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 10, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(java.util.List.of());
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(java.util.List.of());

        ReservationDetailProjection reservation = reservationDetail(
                2L, MEMBER_ID, LocalDate.of(2026, 5, 4), 1L, 1L, LocalTime.of(10, 0)
        );
        WaitingDetailProjection waiting = new WaitingDetailProjection(
                20L, "member", LocalDate.of(2026, 5, 4), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(9, 0), 2L
        );

        when(reservationRepository.findPastReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(java.util.List.of(reservation));
        when(waitingRepository.findPastWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(java.util.List.of(waiting));

        List<ReservationDetailFindResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(MEMBER_ID, ReservationPeriod.HISTORY);

        assertThat(result).hasSize(2);
        verify(reservationRepository).findPastReservationDetailsByMemberId(MEMBER_ID, now);
        verify(waitingRepository).findPastWaitingDetailsByMemberId(MEMBER_ID, now);
    }

    @Test
    @DisplayName("동일한 날짜와 시간에서는 예약(RESERVED)이 대기(WAITING)보다 먼저 조회된다.")
    void findMyReservationsAndWaitingsByPeriod_sameDateTime_reservedFirst() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 9, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation = reservationDetail(
                100L, MEMBER_ID, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(10, 0)
        );
        WaitingDetailProjection waiting = new WaitingDetailProjection(
                200L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(10, 0), 1L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of(reservation));
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of(waiting));

        List<ReservationDetailFindResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(MEMBER_ID, ReservationPeriod.UPCOMING);

        assertThat(result).extracting(ReservationDetailFindResponse::status)
                .containsExactly(ReservationStatus.RESERVED, ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("UPCOMING 조회는 날짜/시간 오름차순이며, 동률이면 예약을 우선한다.")
    void findMyReservationsAndWaitingsByPeriod_upcoming_sortedByDateAndTime() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 9, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation1 = reservationDetail(
                101L, MEMBER_ID, LocalDate.of(2026, 5, 6), 1L, 1L, LocalTime.of(10, 0)
        );
        ReservationDetailProjection reservation2 = reservationDetail(
                102L, MEMBER_ID, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(12, 0)
        );
        WaitingDetailProjection waiting1 = new WaitingDetailProjection(
                201L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(10, 0), 1L
        );
        WaitingDetailProjection waiting2 = new WaitingDetailProjection(
                202L, "member", LocalDate.of(2026, 5, 6), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(9, 0), 2L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of(reservation1, reservation2));
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of(waiting1, waiting2));

        List<ReservationDetailFindResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(MEMBER_ID, ReservationPeriod.UPCOMING);

        assertThat(result).extracting(ReservationDetailFindResponse::id)
                .containsExactly(201L, 102L, 202L, 101L);
    }

    @Test
    @DisplayName("HISTORY 조회는 날짜/시간 내림차순이며, 동률이면 예약을 우선한다.")
    void findMyReservationsAndWaitingsByPeriod_history_sortedByDateAndTime() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 7, 10, 0);
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReservationDetailProjection reservation1 = reservationDetail(
                301L, MEMBER_ID, LocalDate.of(2026, 5, 6), 1L, 1L, LocalTime.of(10, 0)
        );
        ReservationDetailProjection reservation2 = reservationDetail(
                302L, MEMBER_ID, LocalDate.of(2026, 5, 5), 1L, 1L, LocalTime.of(12, 0)
        );
        WaitingDetailProjection waiting1 = new WaitingDetailProjection(
                401L, "member", LocalDate.of(2026, 5, 6), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(12, 0), 1L
        );
        WaitingDetailProjection waiting2 = new WaitingDetailProjection(
                402L, "member", LocalDate.of(2026, 5, 5), 1L, "theme", "description",
                "thumbnail", 1L, LocalTime.of(9, 0), 2L
        );

        when(reservationRepository.findUpcomingReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of());
        when(waitingRepository.findUpcomingWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of());
        when(reservationRepository.findPastReservationDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of(reservation1, reservation2));
        when(waitingRepository.findPastWaitingDetailsByMemberId(MEMBER_ID, now))
                .thenReturn(List.of(waiting1, waiting2));

        List<ReservationDetailFindResponse> result =
                reservationService.findMyReservationsAndWaitingsByPeriod(MEMBER_ID, ReservationPeriod.HISTORY);

        assertThat(result).extracting(ReservationDetailFindResponse::id)
                .containsExactly(401L, 301L, 302L, 402L);
    }
}
