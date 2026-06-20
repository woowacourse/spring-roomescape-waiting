package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.repository.ReservationRepository;
import roomescape.repository.SlotRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWriter {

    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;
    private final MemberResolver memberResolver;

    public ReservationWriter(ReservationRepository reservationRepository, SlotRepository slotRepository,
        MemberResolver memberResolver) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
        this.memberResolver = memberResolver;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation save(String name, Slot slot) {
        Member member = memberResolver.resolve(name);
        Slot foundSlot = slotRepository.getOrCreate(slot);

        Reservation reservationWithSlot = new Reservation(member, foundSlot);

        return reservationRepository.save(reservationWithSlot);
    }
}
