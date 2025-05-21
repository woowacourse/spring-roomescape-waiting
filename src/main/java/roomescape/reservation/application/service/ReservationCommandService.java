package roomescape.reservation.application.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;

    public ReservationCommandService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void deleteById(final Long id) {
        validateExistsReservation(id);
        reservationRepository.deleteById(id);
    }

    private void validateExistsReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new BusinessException("해당 예약이 존재하지 않습니다.");
        }
    }
}
