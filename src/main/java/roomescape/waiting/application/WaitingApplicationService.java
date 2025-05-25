package roomescape.waiting.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.bookingslot.domain.service.ReservationDomainService;
import roomescape.bookingslot.presentation.dto.response.WaitingReservationResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.service.WaitingDomainService;
import roomescape.waiting.presentation.dto.WaitingResponse;

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
        BookingSlot bookingSlot = reservationDomainService.getReservationByDateAndTimeAndTheme(date, timeId, themeId);
        Member member = memberDomainService.getMember(memberId);
        Waiting waiting = bookingSlot.addMemberToWaiting(member);
        waitingDomainService.save(waiting);

        return WaitingReservationResponse.from(waiting);
    }

    public void removeWaiting(final Long reservationId, final Long memberId) {
        waitingDomainService.deleteByBookingSlotIdAndMemberId(reservationId, memberId);
    }

    public List<WaitingResponse> findAllWaitingReservations() {
        List<Waiting> waitings = waitingDomainService.findAllWaitingReservations();
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void removeWaitingReservation(final Long waitingId) {
        waitingDomainService.removeWaitingReservation(waitingId);
    }
}
