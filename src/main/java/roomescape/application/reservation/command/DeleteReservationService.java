package roomescape.application.reservation.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.repository.ReservationRepository;

@Service
@Transactional
public class DeleteReservationService {

    private final ReservationRepository reservationRepository;

    public DeleteReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void removeById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
