package roomescape.waiting.application;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.service.ReservationDomainService;
import roomescape.reservation.presentation.dto.response.WaitingReservationResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.service.WaitingDomainService;

@Service
public class WaitingApplicationService {

    private final MemberDomainService memberDomainService;
    private final ReservationDomainService reservationDomainService;
    private final WaitingDomainService waitingDomainService;

    public WaitingApplicationService(final MemberDomainService memberDomainService,
                                     final ReservationDomainService reservationDomainService,
                                     final WaitingDomainService waitingDomainService) {
        this.memberDomainService = memberDomainService;
        this.reservationDomainService = reservationDomainService;
        this.waitingDomainService = waitingDomainService;
    }

    public WaitingReservationResponse addWaiting(final LocalDate date, final Long timeId, final Long themeId,
                                                 final Long memberId) {
        Reservation reservation = reservationDomainService.getReservationByDateAndTimeAndTheme(date, timeId, themeId);
        Member member = memberDomainService.getMember(memberId);
        Waiting waiting = reservation.addMemberToWaiting(member);
        waitingDomainService.save(waiting);

        return WaitingReservationResponse.from(waiting);
    }

    public void removeWaiting(final Long reservationId, final Long memberId) {
        waitingDomainService.deleteByReservationIdAndMemberId(reservationId, memberId);
    }
}
