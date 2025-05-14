package roomescape.reservation.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationServiceFacade {
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    @Autowired
    public ReservationServiceFacade(
        ReservationService reservationService,
        MemberService memberService,
        ReservationTimeService reservationTimeService,
        ThemeService themeService
    ) {
        this.reservationService = reservationService;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    public ReservationResponse createReservation(
        ReservationCreateRequest reservationCreateRequest,
        MemberPrincipal memberPrincipal
    ) {
        ReservationTime reservationTime = reservationTimeService.findById(reservationCreateRequest.timeId())
            .orElseThrow(() -> new BadRequestException("올바른 예약 시간을 찾을 수 없습니다."));

        Theme theme = themeService.findById(reservationCreateRequest.themeId())
            .orElseThrow(() -> new BadRequestException("올바른 방탈출 테마가 없습니다."));

        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);

        List<ReservationTime> availableTimes = reservationTimeService.findByReservationDateAndThemeId(
            reservationCreateRequest.date(),
            reservationCreateRequest.themeId()
        );
        return reservationService.createReservation(
            reservationTime,
            theme,
            member,
            availableTimes,
            reservationCreateRequest
        );
    }

    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    public List<ReservationResponse> findByCondition(
        ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        return reservationService.findByCondition(reservationSearchConditionRequest);
    }

    public void deleteReservationById(Long id) {
        reservationService.deleteReservationById(id);
    }

    public boolean existsByTimeId(Long id) {
        return reservationService.existsByTimeId(id);
    }
}
