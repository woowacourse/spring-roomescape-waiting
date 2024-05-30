package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.service.exception.ForbiddenOperationCustomException;

@Service
public class ReservationDeletionService {

    private final ReservationRepository reservationRepository;

    public ReservationDeletionService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void deleteByMember(Long id, Member member) {
        Reservation reservation = reservationRepository.getReservationById(id);
        if (reservation.isNotReservedBy(member)) {
            throw new ForbiddenOperationCustomException("자신의 예약이 아닌 것은 삭제할 수 없습니다.");
        }

        reservationRepository.delete(reservation);
    }

    @Transactional
    public void deleteByAdmin(Long id, Member member) {
        if(member.isNotAdmin()) {
            throw new ForbiddenOperationCustomException("관리자만 실행할 수 있습니다.");
        }
        makeFirstWaitingReserved(id);

        reservationRepository.deleteById(id);
    }

    private void makeFirstWaitingReserved(Long id) {
        reservationRepository.getFirstReservationWaiting(id)
                .ifPresent(reservation -> reservation.setStatus(Reservation.Status.RESERVED));
    }
}
