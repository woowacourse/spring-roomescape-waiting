package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundReservationException;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationWithWaitingResult;

@Service
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

    public List<ReservationWithWaitingResult> getMemberReservationsById(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

        return reservations.stream()
                .map(reservation -> ReservationWithWaitingResult.from(
                        reservation, calculateWaitingRank(reservation)
                ))
                .toList();
    }

    private int calculateWaitingRank(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            return 0;
        }
        return reservationRepository.countBeforeWaitings(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getTime().getId(),
                reservation.getId()
        ) + 1;
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
