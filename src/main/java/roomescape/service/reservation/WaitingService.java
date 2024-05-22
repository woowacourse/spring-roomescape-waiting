package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.service.reservation.dto.ReservationResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private final ReservationRepository reservationRepository;

    public WaitingService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponse::new)
                .toList();
    }
}
