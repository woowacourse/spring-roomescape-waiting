package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.repository.ReservationRepository;
import roomescape.repository.SlotRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWriter {

    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;

    public ReservationWriter(ReservationRepository reservationRepository, SlotRepository slotRepository) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation save(Reservation reservation) {
        Slot slot = slotRepository.getOrCreate(reservation.getSlot());
        Reservation reservationWithSlot = new Reservation(reservation.getName(), slot);

        return reservationRepository.save(reservationWithSlot);
    }
}
