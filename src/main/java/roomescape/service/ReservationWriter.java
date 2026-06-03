package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWriter {

    private final ReservationRepository reservationRepository;

    public ReservationWriter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation save(Reservation reservation) {
        Long savedId = reservationRepository.save(reservation);

        return reservationRepository.getById(savedId, "존재하지 않는 예약입니다.");
    }
}
