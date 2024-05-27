package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationStatus;
import roomescape.service.exception.ForbiddenOperationCustomException;
import roomescape.service.exception.ResourceNotFoundCustomException;

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
        findValidatedReservation(id);
        updateFirstWaitingReservationIfAny(id);
        reservationRepository.deleteById(id);
    }

    private Reservation findValidatedReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundCustomException("아이디에 해당하는 예약을 찾을 수 없습니다."));
    }

    private void updateFirstWaitingReservationIfAny(Long id) {
        List<Reservation> waitings = reservationRepository.findReservationsWithSameDateThemeTimeAndStatus(id, ReservationStatus.WAITING);
        if(!waitings.isEmpty()) {
            Reservation firstWaiting = waitings.get(0);
            firstWaiting.setStatus(ReservationStatus.RESERVED);
        }
    }
}
