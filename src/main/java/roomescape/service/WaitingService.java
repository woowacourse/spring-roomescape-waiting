package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.repository.ReservationRepository;

@Transactional
@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;

    public WaitingService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void rejectReservationWaiting(final Long id) {
        final boolean isExist = reservationRepository.existsById(id);
        if (!isExist) {
            throw new IllegalArgumentException("해당 ID의 예약 대기가 없습니다.");
        }
        reservationRepository.deleteById(id);
    }
}
