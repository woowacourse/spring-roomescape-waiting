package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingReservation;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingReservationService {
    private final ReservationRepository reservationRepository;

    public WaitingReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<WaitingReservation> findWaitingReservationsWithPreviousCountByMember(Member member) {
        return reservationRepository.findWaitingReservationsByMemberWithDetails(member);
    }

    public List<Reservation> findWaitingReservations() {
        return reservationRepository.findAllByStatusWithDetails(ReservationStatus.WAITING);
    }

    @Transactional
    public void deleteWaitingReservationByMember(Long reservationId, Member member) {
        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            validateInWaiting(reservation);
            validateOwnerShip(reservation, member);
            reservationRepository.delete(reservation);
        });
    }

    private void validateOwnerShip(Reservation reservation, Member member) {
        Long memberId = member.getId();
        Long ownerId = reservation.getMember().getId();
        if (!memberId.equals(ownerId)) {
            throw new ViolationException("본인의 예약 대기만 삭제할 수 있습니다.");
        }
    }

    @Transactional
    public void deleteWaitingReservationByAdmin(Long reservationId) {
        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            validateInWaiting(reservation);
            reservationRepository.delete(reservation);
        });
    }

    private void validateInWaiting(Reservation reservation) {
        if (!reservation.isWaiting()) {
            throw new ViolationException("대기 중인 예약이 아닙니다.");
        }
    }
}
