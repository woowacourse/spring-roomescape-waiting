package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
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
        Reservations existing = reservationRepository.findBySlotId(slot.getId());

        existing.conflictByName(assembled);

        return reservationRepository.save(assembled.withStatus(existing.nextStatus()));
    }

    public Reservation find(long id) {
        Reservation reservation = reservationRepository.getById(id);

        if (reservation.isWaiting()) {
            Reservations slotReservations = reservationRepository.findBySlotId(reservation.getSlotId());
            return reservation.withRank(slotReservations.rankOf(reservation));
        }

        return reservation;
    }

    public Reservations findAll(String name) {
        if (name == null) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findByName(name);
    }

    @Transactional
    public Reservation update(ReservationUpdateCommand command, long id) {
        Reservation existing = reservationRepository.getById(id);
        Reservation assembled = assembler.from(command);
        Slot newSlot = assembled.getSlot();

        Reservations slotReservations = reservationRepository.findBySlotId(newSlot.getId()).excluding(id);
        slotReservations.conflictByName(assembled);

        Reservation updated = assembled.withStatus(slotReservations.nextStatus());
        reservationRepository.update(id, updated);

        boolean slotChanged = !existing.getSlotId().equals(newSlot.getId());
        if (slotChanged && existing.isApproved()) {
            promoteFirstWaiting(existing.getSlotId());
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
        reservationRepository.findBySlotId(slotId)
                .firstWaiting()
                .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
    }
}
