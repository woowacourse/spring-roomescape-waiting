package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationStatus;
import roomescape.service.exception.ForbiddenOperationCustomException;

import java.util.List;

@Service
public class ReservationDeletionService {

    private final ReservationRepository reservationRepository;

    public ReservationDeletionService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void deleteById(Long id, Member member) {
        validateMemberMadeReservation(id, member);
        this.deleteById(id);
    }

    private void validateMemberMadeReservation(Long id, Member member) {
        if (!reservationRepository.existsByIdAndMemberId(id, member.getId())) {
            throw new ForbiddenOperationCustomException("예약 삭제 권한이 없습니다.");
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservation = reservationRepository.getReservationById(id);
        updateFirstWaitingReservationIfAny(id);
        reservationRepository.delete(reservation);
    }

    private void updateFirstWaitingReservationIfAny(Long id) {
        List<Reservation> waitings = reservationRepository.findReservationsWithSameDateThemeTimeAndStatusOrderedById(id, ReservationStatus.WAITING);
        if(!waitings.isEmpty()) {
            Reservation firstWaiting = waitings.get(0);
            firstWaiting.setStatus(ReservationStatus.RESERVED);
        }
    }
}
