package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.service.exception.ResourceNotFoundCustomException;

@Service
public class ReservationDeletionService {

    private final ReservationRepository reservationRepository;

    public ReservationDeletionService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void deleteById(Long id) {
        findValidatedReservation(id);
        reservationRepository.deleteById(id);
    }

    private void findValidatedReservation(Long id) {
        reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundCustomException("아이디에 해당하는 예약을 찾을 수 없습니다."));
    }
}
