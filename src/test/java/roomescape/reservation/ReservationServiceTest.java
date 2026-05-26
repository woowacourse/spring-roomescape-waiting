package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.member.Role;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.schedule.application.ScheduleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private ReservationService reservationService;

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
                "password",
                Role.USER,
                date,
                themeId,
                "theme",
                "description",
                "thumbnail",
                timeId,
                startAt
        );
    }
}
