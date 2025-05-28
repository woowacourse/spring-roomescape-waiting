package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.service.ScheduleService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

import java.util.List;

@Service
public class AdminReservationFacade {
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ScheduleService scheduleService;

    public AdminReservationFacade(ReservationService reservationService, MemberService memberService,
                                  ReservationTimeService reservationTimeService, ThemeService themeService,
                                  ScheduleService scheduleService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public ReservationResponse create(AdminReservationCreateRequest adminReservationCreateRequest) {
        ReservationTime reservationTime = reservationTimeService.getReservationTimeByTimeId(adminReservationCreateRequest.timeId());
        Theme theme = themeService.getByThemeId(adminReservationCreateRequest.themeId());
        Member member = memberService.getExistingMemberByMemberId(adminReservationCreateRequest.memberId());

        Schedule savedSchedule = scheduleService.createSchedule(adminReservationCreateRequest.date(), reservationTime, theme);

        List<ReservationTime> availableTimes = reservationTimeService.findByReservationTimes(
                adminReservationCreateRequest.date(),
                adminReservationCreateRequest.themeId()
        );

        return reservationService.createAdminReservation(adminReservationCreateRequest, member, availableTimes, savedSchedule);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findByCondition(
            ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        return reservationService.findByCondition(reservationSearchConditionRequest);
    }
}
