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

import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.UserReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Member;
import roomescape.domain.Schedule;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;
import roomescape.service.dto.ReservationWithWaitingOrder;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ReservationService reservationService;

    @DisplayName("빈 슬롯에 예약을 생성하면 RESERVED 상태로 저장한다.")
    @Test
    void saveReservedReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Member member = member(1L, "러로");
        AdminReservationRequest request = new AdminReservationRequest(member.getId(), schedule.getDate(), 1L, 1L);
        given(memberDao.findById(member.getId())).willReturn(Optional.of(member));
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(0);
        given(reservationDao.save(any(Reservation.class))).willReturn(1L);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        Long reservationId = reservationService.saveReservationByAdmin(request);

        assertThat(reservationId).isEqualTo(1L);
        verify(reservationDao).save(reservationCaptor.capture());
        Reservation saved = reservationCaptor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getSchedule()).isEqualTo(schedule);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @DisplayName("이미 예약이 있는 슬롯에 다른 사용자가 신청하면 WAITING 상태로 저장한다.")
    @Test
    void saveWaitingReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Member member = member(2L, "현미밥");
        AdminReservationRequest request = new AdminReservationRequest(member.getId(), schedule.getDate(), 1L, 1L);
        given(memberDao.findById(member.getId())).willReturn(Optional.of(member));
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(1);
        given(reservationDao.save(any(Reservation.class))).willReturn(2L);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.saveReservationByAdmin(request);

        verify(reservationDao).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @DisplayName("로그인 회원으로 예약을 생성하면 회원 조회 없이 저장한다.")
    @Test
    void saveReservationByAdminByLoginMember() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Member member = member(1L, "러로");
        UserReservationRequest request = new UserReservationRequest(schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(0);
        given(reservationDao.save(any(Reservation.class))).willReturn(1L);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.saveReservationByMember(request, member);

        verify(reservationDao).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getMember()).isEqualTo(member);
        verify(memberDao, never()).findById(any());
    }

    @DisplayName("같은 사용자가 같은 슬롯에 취소되지 않은 예약을 가지고 있으면 생성할 수 없다.")
    @Test
    void saveDuplicateReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Member member = member(1L, "러로");
        AdminReservationRequest request = new AdminReservationRequest(member.getId(), schedule.getDate(), 1L, 1L);
        given(memberDao.findById(member.getId())).willReturn(Optional.of(member));
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(true);

        assertThatThrownBy(() -> reservationService.saveReservationByAdmin(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION);

        verify(reservationDao, never()).save(any());
    }

    @DisplayName("과거 스케줄로는 예약을 생성할 수 없다.")
    @Test
    void savePastReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().minusDays(1), LocalTime.of(10, 0));
        Member member = member(1L, "러로");
        AdminReservationRequest request = new AdminReservationRequest(member.getId(), schedule.getDate(), 1L, 1L);
        given(memberDao.findById(member.getId())).willReturn(Optional.of(member));
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(0);

        assertThatThrownBy(() -> reservationService.saveReservationByAdmin(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAST_RESERVATION);
    }

    @DisplayName("RESERVED 예약을 취소하면 취소 시각을 기록하고 첫 번째 대기를 승격한다.")
    @Test
    void cancelReservedReservationPromotesFirstWaiting() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Member member = member(1L, "러로");
        Reservation reserved = reservation(1L, member, schedule, ReservationStatus.RESERVED, LocalDateTime.now().minusHours(2));
        Reservation waiting = reservation(2L, member(2L, "현미밥"), schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findScheduleIdById(1L)).willReturn(Optional.of(schedule.getId()));
        given(reservationDao.findById(1L)).willReturn(Optional.of(reserved));
        given(reservationDao.findFirstByScheduleIdAndStatus(schedule.getId(), ReservationStatus.WAITING))
                .willReturn(Optional.of(waiting));
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.cancelReservation(1L, member);

        verify(reservationDao).changeStatusWithUpdateAt(reservationCaptor.capture());
        Reservation changed = reservationCaptor.getValue();
        assertThat(changed.getId()).isEqualTo(1L);
        assertThat(changed.getMember()).isEqualTo(member);
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
        Member member = member(2L, "현미밥");
        Reservation waiting = reservation(2L, member, schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findScheduleIdById(2L)).willReturn(Optional.of(schedule.getId()));
        given(reservationDao.findById(2L)).willReturn(Optional.of(waiting));
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.cancelReservation(2L, member);

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
        Member member = member(1L, "러로");
        Reservation canceled = reservation(1L, member, schedule, ReservationStatus.CANCELED, LocalDateTime.now().minusHours(1));
        given(reservationDao.findScheduleIdById(1L)).willReturn(Optional.of(schedule.getId()));
        given(reservationDao.findById(1L)).willReturn(Optional.of(canceled));

        reservationService.cancelReservation(1L, member);

        verify(reservationDao, never()).changeStatusWithUpdateAt(any(Reservation.class));
        verify(reservationDao, never()).promoteToReserved(any());
    }

    @DisplayName("본인 예약이 아니면 취소할 수 없다.")
    @Test
    void cancelOtherUserReservation() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation reservation = reservation(1L, member(1L, "러로"), schedule, ReservationStatus.RESERVED, LocalDateTime.now().minusHours(1));
        Member other = member(2L, "다른사람");
        given(reservationDao.findScheduleIdById(1L)).willReturn(Optional.of(schedule.getId()));
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(1L, other))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.UNAUTHORIZED_RESERVATION);

        verify(reservationDao, never()).changeStatusWithUpdateAt(any(Reservation.class));
        verify(reservationDao, never()).promoteToReserved(any());
    }

    @DisplayName("존재하지 않는 예약을 취소하면 예외를 던진다.")
    @Test
    void cancelNotFoundReservation() {
        Member member = member(1L, "러로");
        given(reservationDao.findScheduleIdById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation(404L, member))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.NOT_FOUND_RESERVATION);
    }

    @DisplayName("관리자는 다른 사용자의 RESERVED 예약을 취소하고 첫 번째 대기를 승격한다.")
    @Test
    void cancelReservationByAdmin() {
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation reserved = reservation(1L, member(1L, "러로"), schedule, ReservationStatus.RESERVED, LocalDateTime.now().minusHours(2));
        Reservation waiting = reservation(2L, member(2L, "현미밥"), schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findScheduleIdById(1L)).willReturn(Optional.of(schedule.getId()));
        given(reservationDao.findById(1L)).willReturn(Optional.of(reserved));
        given(reservationDao.findFirstByScheduleIdAndStatus(schedule.getId(), ReservationStatus.WAITING))
                .willReturn(Optional.of(waiting));

        reservationService.cancelReservationByAdmin(1L);

        verify(reservationDao).changeStatusWithUpdateAt(any(Reservation.class));
        verify(reservationDao).promoteToReserved(2L);
    }

    @DisplayName("로그인 회원의 예약 목록은 회원 ID로 조회한다.")
    @Test
    void findByMember() {
        Member member = member(1L, "러로");
        Schedule schedule = futureSchedule(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        Reservation reservation = reservation(1L, member, schedule, ReservationStatus.WAITING, LocalDateTime.now().minusHours(1));
        given(reservationDao.findByMemberId(member.getId())).willReturn(List.of(new ReservationWithWaitingOrder(reservation, 1)));

        List<ReservationWithWaitingOrder> results = reservationService.findByMember(member);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reservation()).isEqualTo(reservation);
        assertThat(results.get(0).order()).isEqualTo(1);
    }

    private Reservation reservation(
            Long id,
            Member member,
            Schedule schedule,
            ReservationStatus status,
            LocalDateTime updatedAt
    ) {
        return new Reservation(id, member, schedule, status, updatedAt);
    }

    private Member member(Long id, String name) {
        return new Member(id, name + "-id", name, "password", Role.USER);
    }

    private Schedule futureSchedule(Long id, LocalDate date, LocalTime time) {
        return new Schedule(
                id,
                new Theme(1L, "테마", "설명", "https://example.com/theme.jpg", 20000),
                date,
                new ReservationTime(1L, time)
        );
    }
}
