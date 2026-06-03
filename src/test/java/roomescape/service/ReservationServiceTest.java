package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.controller.dto.DisplayStatus;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reserver;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationDao;
import roomescape.service.dto.ReservationInfoResult;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ReservationService reservationService;

    @DisplayName("빈 슬롯에 예약을 생성하면 RESERVED 상태로 저장한다.")
    @Test
    void saveReservedReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        ReservationRequest request = new ReservationRequest("러로", schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByNameAndScheduleId("러로", schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(0);
        given(reservationDao.save(any(Reservation.class))).willReturn(1L);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        Long reservationId = reservationService.saveReservation(request);

        assertThat(reservationId).isEqualTo(1L);
        verify(reservationDao).save(reservationCaptor.capture());
        Reservation saved = reservationCaptor.getValue();
        assertThat(saved.getReserver().getName()).isEqualTo("러로");
        assertThat(saved.getSchedule()).isEqualTo(schedule);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @DisplayName("이미 예약이 있는 슬롯에 다른 사용자가 신청하면 WAITING 상태로 저장한다.")
    @Test
    void saveWaitingReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        ReservationRequest request = new ReservationRequest("현미밥", schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByNameAndScheduleId("현미밥", schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(1);
        given(reservationDao.save(any(Reservation.class))).willReturn(2L);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.saveReservation(request);

        verify(reservationDao).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @DisplayName("같은 사용자가 같은 슬롯에 취소되지 않은 예약을 가지고 있으면 생성할 수 없다.")
    @Test
    void saveDuplicateReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        ReservationRequest request = new ReservationRequest("러로", schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByNameAndScheduleId("러로", schedule.getId())).willReturn(true);

        assertThatThrownBy(() -> reservationService.saveReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION);

        verify(reservationDao, never()).save(any());
    }

    @DisplayName("과거 스케줄로는 예약을 생성할 수 없다.")
    @Test
    void savePastReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().minusDays(1), LocalTime.of(10, 0));
        ReservationRequest request = new ReservationRequest("러로", schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByNameAndScheduleId("러로", schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(0);

        assertThatThrownBy(() -> reservationService.saveReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAST_RESERVATION);
    }

    @DisplayName("RESERVED 예약을 취소하면 취소 시각을 기록하고 첫 번째 대기를 승격한다.")
    @Test
    void cancelReservedReservationPromotesFirstWaiting() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation reserved = reservation(1L, "러로", schedule, ReservationStatus.RESERVED, LocalDateTime.now().minusHours(2));
        Reservation waiting = reservation(2L, "현미밥", schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findById(1L)).willReturn(Optional.of(reserved));
        given(reservationDao.findFirstByScheduleIdAndStatus(schedule.getId(), ReservationStatus.WAITING))
                .willReturn(Optional.of(waiting));
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.cancelReservation(1L, "러로");

        verify(reservationDao).changeStatusWithUpdateAt(reservationCaptor.capture());
        Reservation changed = reservationCaptor.getValue();
        assertThat(changed.getId()).isEqualTo(1L);
        assertThat(changed.getReserver().getName()).isEqualTo("러로");
        assertThat(changed.getSchedule()).isEqualTo(schedule);
        assertThat(changed.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(changed.getUpdateAt()).isAfter(reserved.getUpdateAt());
        verify(scheduleService).lockById(schedule.getId());
        verify(reservationDao).promoteToReserved(2L);
    }

    @DisplayName("WAITING 예약을 취소하면 다른 대기를 승격하지 않는다.")
    @Test
    void cancelWaitingReservationDoesNotPromote() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation waiting = reservation(2L, "현미밥", schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findById(2L)).willReturn(Optional.of(waiting));
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.cancelReservation(2L, "현미밥");

        verify(reservationDao).changeStatusWithUpdateAt(reservationCaptor.capture());
        Reservation changed = reservationCaptor.getValue();
        assertThat(changed.getId()).isEqualTo(2L);
        assertThat(changed.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        verify(reservationDao, never()).findFirstByScheduleIdAndStatus(anyLong(), any(ReservationStatus.class));
        verify(reservationDao, never()).promoteToReserved(any(Long.class));
    }

    @DisplayName("이미 취소된 예약은 추가 변경 없이 반환한다.")
    @Test
    void cancelAlreadyCanceledReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation canceled = reservation(1L, "러로", schedule, ReservationStatus.CANCELED, LocalDateTime.now().minusHours(1));
        given(reservationDao.findById(1L)).willReturn(Optional.of(canceled));

        reservationService.cancelReservation(1L, "러로");

        verify(reservationDao, never()).changeStatusWithUpdateAt(any(Reservation.class));
        verify(reservationDao, never()).promoteToReserved(any());
    }

    @DisplayName("본인 예약이 아니면 취소할 수 없다.")
    @Test
    void cancelOtherUserReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation reservation = reservation(1L, "러로", schedule, ReservationStatus.RESERVED, LocalDateTime.now().minusHours(1));
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(1L, "다른사람"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.UNAUTHORIZED_RESERVATION);

        verify(reservationDao, never()).changeStatusWithUpdateAt(any(Reservation.class));
        verify(reservationDao, never()).promoteToReserved(any());
    }

    @DisplayName("존재하지 않는 예약을 취소하면 예외를 던진다.")
    @Test
    void cancelNotFoundReservation() {
        given(reservationDao.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation(404L, "러로"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.NOT_FOUND_RESERVATION);
    }

    @DisplayName("내 예약 목록은 예약별 대기 순번을 포함해 응답으로 변환한다.")
    @Test
    void findByName() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation reservation = reservation(1L, "러로", schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findByName("러로")).willReturn(List.of(new ReservationInfoResult(reservation, 2)));

        List<ReservationResponse> responses = reservationService.findByName("러로");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).reservationId()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("러로");
        assertThat(responses.get(0).status()).isEqualTo(DisplayStatus.WAITING);
        assertThat(responses.get(0).order()).isEqualTo(2);
    }

    @DisplayName("지난 시간의 대기 예약은 EXPIRED 상태로 응답한다.")
    @Test
    void findByNameExpiredWaiting() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().minusDays(1), LocalTime.of(10, 0));
        Reservation reservation = reservation(1L, "러로", schedule, ReservationStatus.WAITING, LocalDateTime.now().minusDays(2));
        given(reservationDao.findByName("러로")).willReturn(List.of(new ReservationInfoResult(reservation, 2)));

        List<ReservationResponse> responses = reservationService.findByName("러로");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(DisplayStatus.EXPIRED);
        assertThat(responses.get(0).order()).isEqualTo(2);
    }

    private Reservation reservation(
            Long id,
            String name,
            Schedule schedule,
            ReservationStatus status,
            LocalDateTime updatedAt
    ) {
        return new Reservation(id, new Reserver(name), schedule, status, updatedAt);
    }

    private Schedule futureSchedule(Long id, LocalDate date, LocalTime time) {
        return new Schedule(
                id,
                new Theme(1L, "테마", "설명", "https://example.com/theme.jpg"),
                date,
                new ReservationTime(1L, time)
        );
    }
}
