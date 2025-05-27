package roomescape.reservation.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
@AllArgsConstructor
public class AdminReservationFacade {

    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    @Transactional
    public ReservationResponse create(AdminReservationCreateRequest adminReservationCreateRequest) {
        ReservationTime reservationTime = reservationTimeService.findByIdOrThrow(
            adminReservationCreateRequest.timeId()
        );
        Theme theme = themeService.findByIdOrThrow(adminReservationCreateRequest.themeId());
        Member member = memberService.findByIdOrThrow(adminReservationCreateRequest.memberId());

        List<ReservationTime> availableTimes = reservationTimeService.findByReservationDateAndThemeId(
            adminReservationCreateRequest.date(),
            adminReservationCreateRequest.themeId()
        );
        return reservationService.create(
            reservationTime,
            adminReservationCreateRequest.date(),
            theme,
            member,
            availableTimes
        );
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByCondition(
        ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        return reservationService.findByCondition(reservationSearchConditionRequest);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findHighestPriorityWaitings() {
        return reservationService.findHighestPriorityWaitings();
    }

    @Transactional
    public void approveWaiting(Long id) {
        reservationService.approveWaiting(id);
    }

    @Transactional
    public void denyWaiting(Long id) {
        reservationService.denyWaiting(id);
    }
}
