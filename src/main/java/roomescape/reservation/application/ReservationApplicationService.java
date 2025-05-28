package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.response.TotalReservationResponse;
import roomescape.reservation.presentation.dto.response.WaitingResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

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

    public List<TotalReservationResponse> findReservations(final Long themeId, final Long memberId,
                                                           final LocalDate startDate,
                                                           final LocalDate endDate) {
        List<Reservation> filteredReservations = reservationDataService.findFilteredReservations(themeId,
                memberId, startDate, endDate);
        return filteredReservations
                .stream()
                .map(this::mapToTotalReservationResponse)
                .toList();
    }

    public ReservationResponse addWaitingReservation(final LocalDate date, final Long timeId, final Long themeId,
                                                     final Long memberId) {
        ReservationSlot reservationSlot = reservationSlotDataService.getReservationByDateAndTimeAndTheme(date, timeId,
                themeId);
        Member member = memberDataService.getMember(memberId);
        Reservation reservation = reservationSlot.addReservation(member, LocalDateTime.now());
        reservationDataService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    public void removeWaiting(final Long reservationSlotId, final Long memberId) {
        reservationDataService.deleteByReservationSlotIdAndMemberId(reservationSlotId, memberId);
        if (reservationDataService.existsByReservationSlotIdAndMemberId(reservationSlotId, memberId)) {
            reservationSlotDataService.delete(reservationSlotId);
        }
    }

    public List<WaitingResponse> findAllWaitingReservations() {
        List<Reservation> reservations = reservationDataService.findAllWaitingReservations();
        return reservations.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void removeWaitingReservation(final Long reservationId) {
        reservationDataService.removeWaitingReservation(reservationId);
    }

    private TotalReservationResponse mapToTotalReservationResponse(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        ReservationTime time = reservationSlot.getTime();
        Theme theme = reservationSlot.getTheme();
        Member member = reservationSlot.findReservedMember();
        return TotalReservationResponse.of(reservation, reservationSlot, time, theme, member);
    }

    public void delete(final Long reservationId) {
        Reservation reservation = reservationDataService.getById(reservationId);
        reservationDataService.deleteById(reservationId);

        deleteReservationSlotIfExists(reservation);
    }

    private void deleteReservationSlotIfExists(final Reservation reservation) {
        Long slotId = reservation.getReservationSlot().getId();
        if (reservationSlotDataService.hasOnlyOneReservation(slotId)) {
            reservationSlotDataService.delete(slotId);
        }
    }
}
