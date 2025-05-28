package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationResponse;
import roomescape.reservation.presentation.dto.response.WaitingResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;

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

    public ReservationResponse createWaitingReservation(final LocalDate date, final Long timeId, final Long themeId,
                                                        final Long memberId) {
        ReservationSlot reservationSlot = reservationSlotDataService.getReservationSlotByDateAndTimeAndTheme(date,
                timeId, themeId);
        Member member = memberDataService.getById(memberId);
        Reservation reservation = reservationSlot.addReservation(member, LocalDateTime.now());
        reservationDataService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    public List<ConfirmedReservationResponse> findByCriteria(
            final Long themeId,
            final Long memberId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        List<Reservation> reservations = reservationDataService.findByCriteria(themeId, memberId, startDate,
                endDate);
        return reservations
                .stream()
                .map(this::mapToConfirmedReservationResponse)
                .toList();
    }

    public List<WaitingResponse> findWaitingReservations() {
        List<Reservation> reservations = reservationDataService.findAllWaitingReservations();
        return reservations.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void removeWaitingReservation(final Long reservationSlotId, final Long memberId) {
        reservationDataService.deleteByReservationSlotIdAndMemberId(reservationSlotId, memberId);
        if (reservationDataService.existsByReservationSlotIdAndMemberId(reservationSlotId, memberId)) {
            reservationSlotDataService.deleteById(reservationSlotId);
        }
    }

    public void removeWaitingReservationWithoutMemberId(final Long reservationId) {
        reservationDataService.removeWaitingReservation(reservationId);
    }

    public void removeById(final Long reservationId) {
        Reservation reservation = reservationDataService.getById(reservationId);
        reservationDataService.deleteById(reservationId);

        deleteReservationSlotIfOnlyOneReservation(reservation);
    }

    private ConfirmedReservationResponse mapToConfirmedReservationResponse(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return ConfirmedReservationResponse.of(reservation, reservationSlot);
    }

    private void deleteReservationSlotIfOnlyOneReservation(final Reservation reservation) {
        Long reservationSlotId = reservation.getReservationSlot().getId();
        if (reservationSlotDataService.hasSingleReservation(reservationSlotId)) {
            reservationSlotDataService.deleteById(reservationSlotId);
        }
    }
}
