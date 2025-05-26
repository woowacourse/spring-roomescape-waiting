package roomescape.reservation.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
@AllArgsConstructor
public class ReservationServiceFacade {

    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationResponse createReservation(
        ReservationCreateRequest reservationCreateRequest,
        MemberPrincipal memberPrincipal
    ) {
        ReservationTime reservationTime = reservationTimeService.findByIdOrThrow(reservationCreateRequest.timeId());
        Theme theme = themeService.findByIdOrThrow(reservationCreateRequest.themeId());
        Member member = memberService.findByPrincipalOrThrow(memberPrincipal);
        List<ReservationTime> availableTimes = reservationTimeService.findByReservationDateAndThemeId(
            reservationCreateRequest.date(),
            reservationCreateRequest.themeId()
        );

        return reservationService.create(
            reservationTime,
            reservationCreateRequest.date(),
            theme,
            member,
            availableTimes
        );
    }

    public ReservationResponse createWaiting(
        ReservationCreateRequest reservationCreateRequest,
        MemberPrincipal memberPrincipal
    ) {
        ReservationTime reservationTime = reservationTimeService.findByIdOrThrow(reservationCreateRequest.timeId());
        Theme theme = themeService.findByIdOrThrow(reservationCreateRequest.themeId());
        Member member = memberService.findByPrincipalOrThrow(memberPrincipal);

        return reservationService.createWaiting(
            reservationCreateRequest.date(),
            reservationTime,
            theme,
            member
        );
    }

    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    public void deleteById(Long id) {
        reservationService.deleteById(id);
    }

    public List<MyReservationResponse> findMine(MemberPrincipal memberPrincipal) {
        Member member = memberService.findByPrincipalOrThrow(memberPrincipal);
        return reservationService.findAllByMember(member);
    }

    public void deleteWaiting(Long id, MemberPrincipal memberPrincipal) {
        Member member = memberService.findByPrincipalOrThrow(memberPrincipal);
        reservationService.deleteWaiting(id, member);
    }
}
