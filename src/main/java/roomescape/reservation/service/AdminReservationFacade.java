package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
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

    public ReservationResponse create(AdminReservationCreateRequest adminReservationCreateRequest) {
        ReservationTime reservationTime = reservationTimeService.findById(adminReservationCreateRequest.timeId())
                .orElseThrow(() -> new BadRequestException("올바른 예약 시간을 찾을 수 없습니다."));

        Theme theme = themeService.findById(adminReservationCreateRequest.themeId())
                .orElseThrow(() -> new BadRequestException("올바른 방탈출 테마가 없습니다."));

        Member member = memberService.findExistingMemberById(adminReservationCreateRequest.memberId());

        Schedule schedule = new Schedule(null, adminReservationCreateRequest.date(), reservationTime, theme);
        Schedule savedSchedule = scheduleService.save(schedule);

        List<ReservationTime> availableTimes = reservationTimeService.findByReservationDateAndThemeId(
                adminReservationCreateRequest.date(),
                adminReservationCreateRequest.themeId()
        );
        return reservationService.createReservation(
                member,
                availableTimes,
                savedSchedule,
                adminReservationCreateRequest.toReservationCreateRequest()
        );
    }

    public List<ReservationResponse> findByCondition(
            ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        return reservationService.findByCondition(reservationSearchConditionRequest);
    }
}
