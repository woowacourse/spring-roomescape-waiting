package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.ReservationRepository;
import roomescape.service.exception.ForbiddenOperationCustomException;
import roomescape.service.exception.ResourceNotFoundCustomException;

@Service
public class ReservationDeletionService {

    private final ReservationRepository reservationRepository;

    public ReservationDeletionService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void deleteById(Long id, Member member) {
        validateMemberMadeReservation(id, member);
        deleteById(id);
    }

    private void validateMemberMadeReservation(Long id, Member member) {
        if (!reservationRepository.existsByIdAndMemberId(id, member.getId())) {
            throw new ForbiddenOperationCustomException("예약 삭제 권한이 없습니다.");
        }
    }

    public void deleteById(Long id) {
        findValidatedReservation(id);
        reservationRepository.deleteById(id);
    }

    private void findValidatedReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundCustomException("아이디에 해당하는 예약을 찾을 수 없습니다.");
        }
    }
}
