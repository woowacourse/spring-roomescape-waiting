package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.order.domain.Order;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.controller.dto.response.ReservationWithSlotDetailDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.slot.domain.RescheduleSlots;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.slot.repository.ReservationSlotRepository;

import java.util.Arrays;
import java.util.List;

import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentService paymentService;

    public List<ReservationWithSlotInformation> readAll() {
        return reservationRepository.findAll();
    }

    public List<ReservationWithSlotInformation> readAllByName(String name) {
        return reservationRepository.findByMemberName(name);
    }

    @Transactional
    public ReservationWithSlotDetailDto reserve(String requesterName, Long slotId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservation reservation = slot.reserve(requesterName);
        Reservation saved = reservationRepository.save(reservation);

        if (saved.isPendingPayment()) {
            Order order = paymentService.createOrder(saved.getId(), slot.getTheme().getAmount());
            return ReservationWithSlotDetailDto.of(saved, slot, order);
        }

        return ReservationWithSlotDetailDto.of(saved, slot);
    }

    @Transactional
    public Reservation cancel(Long slotId, Long reservationId, String requesterName) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancel(reservationId, requesterName);
        cancelAndPromote(changed);
        return changed.findById(reservationId);
    }

    @Transactional
    public Reservation cancelByManager(Long slotId, Long reservationId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancelByManager(reservationId);
        cancelAndPromote(changed);
        return changed.findById(reservationId);
    }

    @Transactional
    public Reservation reschedule(Long currentSlotId, Long newSlotId, Long reservationId, String requesterName) {
        RescheduleSlots slots = getRescheduleSlotsWithLock(currentSlotId, newSlotId);
        Reservations changed = slots.reschedule(currentSlotId, newSlotId, reservationId, requesterName);
        rescheduleAndPromote(changed);
        return changed.findById(reservationId);
    }

    @Transactional
    public Reservation rescheduleByManager(Long currentSlotId, Long newSlotId, Long reservationId) {
        RescheduleSlots slots = getRescheduleSlotsWithLock(currentSlotId, newSlotId);
        Reservations changed = slots.rescheduleByManager(currentSlotId, newSlotId, reservationId);
        rescheduleAndPromote(changed);
        return changed.findById(reservationId);
    }

    private RescheduleSlots getRescheduleSlotsWithLock(Long... slotIds) {
        List<ReservationSlot> rescheduleSlots = Arrays.stream(slotIds)
                .sorted()
                .map(this::getSlotAndReservationsWithLock)
                .toList();

        return RescheduleSlots.of(rescheduleSlots);
    }

    private ReservationSlot getSlotAndReservationsWithLock(Long slotId) {
        ReservationSlot slot = getSlotWithLock(slotId);
        List<Reservation> activeReservations = getReservationsOfSlot(slot);
        return slot.withReservations(new Reservations(activeReservations));
    }

    private ReservationSlot getSlotWithLock(Long slotId) {
        return reservationSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
    }

    private List<Reservation> getReservationsOfSlot(ReservationSlot slot) {
        return reservationRepository.findReservedAndWaitingBySlotId(slot.getId());
    }

    public void cancelAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateStatus);
    }

    private void rescheduleAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateSchedule);
    }

}
