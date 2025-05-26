package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.service.ScheduleService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.AdminWaitingResponse;
import roomescape.waiting.dto.response.WaitingCreateResponse;

import java.util.List;

@Service
public class WaitingServiceFacade {
    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;
    private final MemberService memberService;
    private final WaitingService waitingService;
    private final ScheduleService scheduleService;

    public WaitingServiceFacade(ReservationService reservationService, ThemeService themeService, ReservationTimeService reservationTimeService, MemberService memberService, WaitingService waitingService, ScheduleService scheduleService) {
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
        this.memberService = memberService;
        this.waitingService = waitingService;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public WaitingCreateResponse createWaiting(WaitingCreateRequest waitingCreateRequest, MemberPrincipal memberPrincipal) {
        ReservationTime reservationTime = reservationTimeService.getReservationTimeByTimeId(waitingCreateRequest.timeId());
        Theme theme = themeService.getByThemeId(waitingCreateRequest.themeId());
        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);
        Schedule schedule = scheduleService.getScheduleByDateAndTimeIdAndThemeId(waitingCreateRequest.date(), reservationTime.getId(), theme.getId());
        validateConflict(member, schedule);
        Waiting waiting = waitingService.createWaiting(waitingCreateRequest, schedule, member);
        return WaitingCreateResponse.from(waiting);
    }

    private void validateConflict(Member member, Schedule schedule) {
        boolean isConflictReservation = reservationService.existsByMemberAndSchedule(member, schedule);
        if (isConflictReservation) {
            throw new ConflictException("내 예약에 대기 요청 할 수 없습니다.");
        }

        boolean isConflictWaiting = waitingService.existsByMemberAndSchedule(member, schedule);
        if (isConflictWaiting) {
            throw new ConflictException("이미 대기 내역이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<AdminWaitingResponse> getAdminWaitings() {
        return waitingService.findAll().stream()
                .map(AdminWaitingResponse::from)
                .toList();
    }

    @Transactional
    public void deleteWaiting(Long waitingId) {
        waitingService.deleteWaitingById(waitingId);
    }

    @Transactional
    public void acceptWaiting(Long waitingId) {
        Schedule schedule = getScheduleByWaitingId(waitingId);
        validateScheduleConflictInReservation(schedule);
        Waiting nextWaitingInfo = waitingService.getFirstWaitingBySchedule(schedule);
        createReservationFromNextWaitingInfo(nextWaitingInfo);
        waitingService.deleteWaitingById(nextWaitingInfo.getId());
    }

    private void createReservationFromNextWaitingInfo(Waiting nextWaitingInfo) {
        Schedule nextWaitingInfoSchedule = nextWaitingInfo.getSchedule();
        ReservationCreateRequest request = new ReservationCreateRequest(nextWaitingInfoSchedule.getDate(), nextWaitingInfoSchedule.getTime().getId(), nextWaitingInfoSchedule.getTheme().getId());
        List<ReservationTime> availableTimes = findAvailableTimes(request);
        reservationService.createReservation(nextWaitingInfo.getMember(), availableTimes, nextWaitingInfoSchedule, request);
    }

    private List<ReservationTime> findAvailableTimes(ReservationCreateRequest request) {
        return reservationTimeService.findByReservationDateAndThemeId(
                request.date(),
                request.themeId()
        );
    }

    private void validateScheduleConflictInReservation(Schedule schedule) {
        boolean existsSchedule = reservationService.existsBySchedule(schedule);
        if (existsSchedule) {
            throw new ConflictException("예약이 존재합니다.");
        }
    }

    private Schedule getScheduleByWaitingId(Long waitingId) {
        Waiting findWaiting = waitingService.getScheduleByWaitingId(waitingId);
        return findWaiting.getSchedule();
    }
}
