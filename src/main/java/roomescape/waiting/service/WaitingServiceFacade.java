package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.service.ScheduleService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.WaitingCreateResponse;

@Service
public class WaitingServiceFacade {
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;
    private final MemberService memberService;
    private final WaitingService waitingService;
    private final ScheduleService scheduleService;

    public WaitingServiceFacade(ThemeService themeService, ReservationTimeService reservationTimeService, MemberService memberService, WaitingService waitingService, ScheduleService scheduleService) {
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
        this.memberService = memberService;
        this.waitingService = waitingService;
        this.scheduleService = scheduleService;
    }

    public WaitingCreateResponse createWaiting(WaitingCreateRequest waitingCreateRequest, MemberPrincipal memberPrincipal) {
        ReservationTime reservationTime = reservationTimeService.findById(waitingCreateRequest.timeId())
                .orElseThrow(() -> new BadRequestException("올바른 예약 시간을 찾을 수 없습니다."));

        Theme theme = themeService.findById(waitingCreateRequest.themeId())
                .orElseThrow(() -> new BadRequestException("올바른 방탈출 테마가 없습니다."));

        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);

        Schedule schedule = scheduleService.findByDateAndTimeIdAndThemeId(waitingCreateRequest.date(), reservationTime.getId(), theme.getId())
                .orElseThrow(() -> new BadRequestException("올바른 예약 일정이 존재하지 않습니다."));

        Waiting waiting = waitingService.createWaiting(waitingCreateRequest, schedule, member);

        return WaitingCreateResponse.from(waiting);
    }

    public void deleteWaiting(Long id) {
        waitingService.deleteWaitingById(id);
    }
}
