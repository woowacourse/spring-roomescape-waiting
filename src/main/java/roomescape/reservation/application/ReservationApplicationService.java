package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.presentation.dto.WaitingResponse;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.member.domain.Member;
import roomescape.member.application.MemberDataService;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;

@Service
public class ReservationApplicationService {

    private final MemberDataService memberDataService;
    private final ReservationSlotDataService reservationSlotDataService;
    private final ReservationDataService reservationDataService;

    public ReservationApplicationService(final MemberDataService memberDataService,
                                         final ReservationSlotDataService reservationSlotDataService,
                                         final ReservationDataService reservationDataService) {
        this.memberDataService = memberDataService;
        this.reservationSlotDataService = reservationSlotDataService;
        this.reservationDataService = reservationDataService;
    }

    public ReservationResponse addWaiting(final LocalDate date, final Long timeId, final Long themeId,
                                          final Long memberId) {
        ReservationSlot reservationSlot = reservationSlotDataService.getReservationByDateAndTimeAndTheme(date, timeId, themeId);
        Member member = memberDataService.getMember(memberId);
        Reservation reservation = reservationSlot.addMemberToWaiting(member);
        reservationDataService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    public void removeWaiting(final Long reservationId, final Long memberId) {
        reservationDataService.deleteByReservationSlotIdAndMemberId(reservationId, memberId);
    }

    public List<WaitingResponse> findAllWaitingReservations() {
        List<Reservation> reservations = reservationDataService.findAllWaitingReservations();
        return reservations.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void removeWaitingReservation(final Long waitingId) {
        reservationDataService.removeWaitingReservation(waitingId);
    }

    public void delete(final Long reservationId) {
        reservationDataService.deleteById(reservationId);
    }
}
