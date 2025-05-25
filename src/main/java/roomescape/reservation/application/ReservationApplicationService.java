package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.domain.service.ReservationSlotDomainService;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.reservationslot.presentation.dto.response.WaitingReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.service.ReservationDomainService;
import roomescape.reservation.presentation.dto.ReservationResponse;

@Service
public class ReservationApplicationService {

    private final MemberDomainService memberDomainService;
    private final ReservationSlotDomainService reservationSlotDomainService;
    private final ReservationDomainService reservationDomainService;

    public ReservationApplicationService(final MemberDomainService memberDomainService,
                                         final ReservationSlotDomainService reservationSlotDomainService,
                                         final ReservationDomainService reservationDomainService) {
        this.memberDomainService = memberDomainService;
        this.reservationSlotDomainService = reservationSlotDomainService;
        this.reservationDomainService = reservationDomainService;
    }

    public WaitingReservationResponse addWaiting(final LocalDate date, final Long timeId, final Long themeId,
                                                 final Long memberId) {
        ReservationSlot reservationSlot = reservationSlotDomainService.getReservationByDateAndTimeAndTheme(date, timeId, themeId);
        Member member = memberDomainService.getMember(memberId);
        Reservation reservation = reservationSlot.addMemberToWaiting(member);
        reservationDomainService.save(reservation);

        return WaitingReservationResponse.from(reservation);
    }

    public void removeWaiting(final Long reservationId, final Long memberId) {
        reservationDomainService.deleteByReservationSlotIdAndMemberId(reservationId, memberId);
    }

    public List<ReservationResponse> findAllWaitingReservations() {
        List<Reservation> reservations = reservationDomainService.findAllWaitingReservations();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void removeWaitingReservation(final Long waitingId) {
        reservationDomainService.removeWaitingReservation(waitingId);
    }
}
