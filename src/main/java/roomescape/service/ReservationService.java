package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundException;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationWithWaitingResult;
import roomescape.service.result.WaitingWithRank;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResult> getReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom,
                                                               LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(memberId, themeId, dateFrom,
                dateTo);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getWaitingReservations() {
        List<Reservation> reservations = reservationRepository.findWaitingReservations();
        return ReservationResult.from(reservations);
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

    @Transactional
    public void deleteById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("reservationId", reservationId));

        reservationRepository.deleteById(reservationId);

        boolean reservationSlotEmpty = reservationRepository.isReservationSlotEmpty(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        if(reservationSlotEmpty) {
            autoReserveNextWaiting(reservation);
        }
    }

    private void autoReserveNextWaiting(Reservation canceled) {
        Optional<Reservation> firstWaiting = reservationRepository.findFirstWaiting(
                canceled.getDate(), canceled.getTheme().getId(), canceled.getTime().getId());

        firstWaiting.ifPresent(waiting -> {
            waiting.changeStatusToReserved();
            reservationRepository.save(waiting);
        });
    }

    @Transactional
    public void cancelWaitingById(Long reservationId, LoginMemberInfo loginMemberInfo) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("reservationId", reservationId));
        validateCancelPermission(loginMemberInfo, reservation);

        reservationRepository.deleteById(reservationId);
    }

    private void validateCancelPermission(LoginMemberInfo loginMemberInfo, Reservation reservation) {
        if (!loginMemberInfo.id().equals(reservation.getMember().getId())) {
            throw new DeletionNotAllowedException("자신의 예약만 삭제할 수 있습니다.");
        }
    }
}
