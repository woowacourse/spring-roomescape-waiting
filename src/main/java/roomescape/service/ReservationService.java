package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final Clock clock;
    private final ReservationAssembler assembler;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            Clock clock,
            ReservationAssembler assembler,
            ReservationRepository reservationRepository
    ) {
        this.clock = clock;
        this.assembler = assembler;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateCommand command) {
        Reservation assembled = assembler.from(command);
        Slot slot = assembled.getSlot();

        Reservations existing = new Reservations(reservationRepository.findBySlot_Id(slot.getId()));
        Reservation join = existing.join(assembled);
        return reservationRepository.save(join);
    }

    public Reservation find(long id) {
        Reservation reservation = reservationRepository.getById(id);
        Reservations slotReservations = new Reservations(reservationRepository.findBySlot_Id(reservation.getSlotId()));
        return reservation.withRank(slotReservations.rankOf(reservation));
    }

    public Reservations findAll(String name) {
        if (name == null) {
            return new Reservations(reservationRepository.findAll());
        }
        return new Reservations(reservationRepository.findAllByName(new ReservationName(name)));
    }

    @Transactional
    public Reservation update(ReservationUpdateCommand command, long id) {
        Reservation existing = reservationRepository.getById(id);
        Reservation assembled = assembler.from(command);

        Slot newSlot = assembled.getSlot();
        Long oldSlotId = existing.getSlotId();

        Reservations slotReservations = new Reservations(reservationRepository.findBySlot_Id(newSlot.getId())).excluding(id);
        Reservation template = slotReservations.join(assembled);

        boolean wasApproved = existing.isApproved();
        existing.changeSlot(template.getSlot());
        existing.changeStatus(template.getStatus());

        boolean slotChanged = !oldSlotId.equals(newSlot.getId());
        if (slotChanged && wasApproved) {
            promoteFirstWaiting(oldSlotId);
        }

        return find(id);
    }

    @Transactional
    public void cancel(long reservationId, String name) {
        Reservation reservation = reservationRepository.getById(reservationId);
        LocalDateTime now = LocalDateTime.now(clock);

        reservation.validateCancellable(now);
        reservation.validateOwner(name);

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            promoteFirstWaiting(reservation.getSlotId());
        }
    }

    private void promoteFirstWaiting(Long slotId) {
        new Reservations(reservationRepository.findBySlot_Id(slotId))
                .firstWaiting()
                .ifPresent(waiting -> waiting.changeStatus(Status.APPROVED));
    }
}
