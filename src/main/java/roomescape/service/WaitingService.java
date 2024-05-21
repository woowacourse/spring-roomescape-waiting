package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;

@Service
@Transactional
public class WaitingService {

    private final ReservationRepository reservationRepository;

    public WaitingService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse create(final Reservation reservation) {
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }
}
