package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public ReservationQueryService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponse> getAllReservedReservations() {
        return reservationRepository.findAll()
                .stream()
                .filter(reservation -> reservation.getReservationStatus().isReserved())
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getAllWaitingReservations() {
        return reservationRepository.findAll()
                .stream()
                .filter(reservation -> reservation.getReservationStatus().isWaiting())
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(Member member) {
        return reservationRepository.findByMemberId(member.getId()).stream()
                .map(this::getMyReservationsWithWaitRank)
                .toList();
    }

    private MyReservationResponse getMyReservationsWithWaitRank(Reservation reservation) {
        long waitingRank = 0L;
        if (reservation.getReservationStatus().isWaiting()) {
            long waitingCountsInFrontOfMe = reservationRepository
                    .countPreviousReservationsWithSameDateThemeTimeAndStatus(reservation.getId(), ReservationStatus.WAITING);
            waitingRank = waitingCountsInFrontOfMe + 1;
        }

        return MyReservationResponse.of(reservation, waitingRank);
    }

    public List<ReservationResponse> getFilteredReservations(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.filter(themeId, memberId, dateFrom, dateTo, ReservationStatus.RESERVED);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
