package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

import static roomescape.domain.Reservation.Status;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public ReservationQueryService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservedReservations() {
        return reservationRepository.findAll()
                .stream()
                .filter(Reservation::isReserved)
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllWaitingReservations() {
        return reservationRepository.findAll()
                .stream()
                .filter(Reservation::isWaiting)
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getMyReservations(Member member) {
        return reservationRepository.findByMemberId(member.getId()).stream()
                .map(this::getMyReservationsWithWaitRank)
                .toList();
    }

    private MyReservationResponse getMyReservationsWithWaitRank(Reservation reservation) {
        long waitingRank = 0L;
        if (reservation.isWaiting()) {
            long waitingCountsInFrontOfMe = reservationRepository
                    .countPreviousReservationsWithSameDateThemeTimeAndStatus(reservation.getId(), Status.WAITING);
            waitingRank = waitingCountsInFrontOfMe + 1;
        }

        return MyReservationResponse.of(reservation, waitingRank);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getFilteredReservations(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.filter(themeId, memberId, dateFrom, dateTo, Status.RESERVED);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
