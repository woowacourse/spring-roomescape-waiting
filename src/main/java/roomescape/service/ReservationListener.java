package roomescape.service;

import jakarta.persistence.PostRemove;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import roomescape.entity.CanceledReservation;
import roomescape.entity.Reservation;
import roomescape.repository.CanceledReservationRepository;

@Component
public class ReservationListener {

    private final CanceledReservationRepository canceledReservationRepository;

    public ReservationListener(@Lazy CanceledReservationRepository canceledReservationRepository) {
        this.canceledReservationRepository = canceledReservationRepository;
    }

    @PostRemove
    @Transactional
    public void onPostRemove(Reservation reservation) {
        CanceledReservation canceledReservation = new CanceledReservation(reservation);
        canceledReservationRepository.save(canceledReservation);
    }
}
