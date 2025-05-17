package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundReservationException;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationWithWaitingResult;
import roomescape.service.result.WaitingWithRank;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResult> getReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationWithWaitingResult> getMemberReservationsById(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        Map<Long, Long> waitingRanks = loadWaitingRanks(memberId);

        return reservations.stream()
                .map(reservation -> ReservationWithWaitingResult.from(
                        reservation,
                        waitingRanks.getOrDefault(reservation.getId(), 0L)
                ))
                .toList();
    }

    private Map<Long, Long> loadWaitingRanks(Long memberId) {
        return reservationRepository.findWaitingsWithRankByMemberId(memberId).stream()
                .map(WaitingWithRank::withPlusOneRank)
                .collect(Collectors.toMap(
                        WaitingWithRank::reservationId,
                        WaitingWithRank::rank
                ));
    }

    public void deleteById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public void deleteWaitingById(Long reservationId, LoginMemberInfo loginMemberInfo) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundReservationException("해당 id의 예약이 존재하지 않습니다."));

        if (!loginMemberInfo.id().equals(reservation.getMember().getId())) {
            throw new DeletionNotAllowedException("자신의 예약만 삭제할 수 있습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }
}
