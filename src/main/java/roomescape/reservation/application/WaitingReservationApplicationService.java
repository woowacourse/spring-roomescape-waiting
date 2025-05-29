package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.WaitingWebResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;

@Service
public class WaitingReservationApplicationService {

    private final ReservationSlotDataService reservationSlotDataService;
    private final MemberDataService memberDataService;
    private final ReservationDataService reservationDataService;

    public WaitingReservationApplicationService(final ReservationSlotDataService reservationSlotDataService,
                                                final MemberDataService memberDataService,
                                                final ReservationDataService reservationDataService) {
        this.reservationSlotDataService = reservationSlotDataService;
        this.memberDataService = memberDataService;
        this.reservationDataService = reservationDataService;
    }

    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId,
                                      final Long memberId) {
        ReservationSlot slot = reservationSlotDataService.getReservationSlotByDateAndTimeAndTheme(date,
                timeId, themeId);
        Member member = memberDataService.getById(memberId);
        Reservation reservation = slot.addReservation(member, LocalDateTime.now());
        reservationDataService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    public List<WaitingWebResponse> findAll() {
        List<Reservation> reservations = reservationDataService.findAllWaitingReservations();
        return reservations.stream()
                .map(WaitingWebResponse::from)
                .toList();
    }

    public void cancel(final Long reservationSlotId, final Long memberId) {
        reservationDataService.deleteByReservationSlotIdAndMemberId(reservationSlotId, memberId);
        if (reservationDataService.existsByReservationSlotIdAndMemberId(reservationSlotId, memberId)) {
            reservationSlotDataService.deleteById(reservationSlotId);
        }
    }

    public void cancelWaitingReservationWithoutMemberId(final Long reservationId) {
        Reservation reservation = reservationDataService.getById(reservationId);
        reservationDataService.cancelWaitingReservation(reservation);
        cleanupEmptyReservationSlot(reservation.getReservationSlot().getId());
    }

    private void cleanupEmptyReservationSlot(final Long slotId) {
        if (reservationSlotDataService.hasSingleReservation(slotId)) {
            reservationSlotDataService.deleteById(slotId);
        }
    }
}
