package roomescape.service.reservation;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.repository.ReservationRepository;

@Service
public class ReservationDeleteService {

    private final ReservationRepository reservationRepository;

    public ReservationDeleteService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void deleteReservation(long id, Member member) {
        Reservation deleteReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 아이디 입니다."));
        if (member.getRole() == Role.USER) {
            validateIsOwnReservation(member, deleteReservation);
        }
        reservationRepository.deleteById(id);

        if (deleteReservation.isReserved()) {
            reservationRepository.findNextWaiting(deleteReservation.getTheme(),
                            deleteReservation.getReservationTime(), deleteReservation.getDate(), Limit.of(1))
                    .ifPresent(reservation -> reservation.changeReservationStatus(ReservationStatus.RESERVED));
        }
    }

    private void validateIsOwnReservation(Member member, Reservation deleteReservation) {
        if (deleteReservation.isNotOwnedBy(member)) {
            throw new AuthenticationException("본인의 예약만 삭제할 수 있습니다.");
        }
    }
}
