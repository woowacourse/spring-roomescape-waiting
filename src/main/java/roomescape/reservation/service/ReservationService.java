package roomescape.reservation.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.slot.repository.ReservationSlotRepository;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;

import java.util.List;

import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_FOUND;
import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
    @RequiredArgsConstructor
public class ReservationService {

    private final ReservationRescheduleService rescheduleService;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public List<Reservation> readAll() {
        return reservationRepository.findAll();
    }

    public List<ReservationWithWaitingTurn> readAllByName(String name) {
        return reservationRepository.findMyReservationsWithWaitingTurn(name);
    }

    @Transactional
    public Reservation reserve(String name, ReservationSaveCommand command) {
        ReservationSlot slot = getSlotWithLock(command.dateId(), command.timeId(), command.themeId());
        Reservations reservationsOfTimeSlot = findTimeSlotReservations(slot);
        Reservation reservation = reservationsOfTimeSlot.reserve(name, slot, LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelByManager(Long id) {
        Reservation reservation = getReservation(id);
        if (reservation.isReserved()) {
            cancelByManger(reservation);
            rescheduleService.rescheduleWaitingOrder(reservation.getSlot());
            return reservation;
        }

        cancelByManger(reservation);
        return reservation;
    }

    @Transactional
    public Reservation cancel(Long id, String requesterName) {
        Reservation reservation = getReservation(id);
        if (reservation.isReserved()) {
            cancel(reservation, requesterName);
            rescheduleService.rescheduleWaitingOrder(reservation.getSlot());
            return reservation;
        }

        cancel(reservation, requesterName);
        return reservation;
    }

    @Transactional
    public Reservation changeSchedule(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationSlot newSlot = getSlot(command.dateId(), command.timeId(), reservation.getSlot().getThemeId());
        validateAlreadyBookedByOthers(newSlot);

        reservation.changeSchedule(command.requesterName(), newSlot, LocalDateTime.now());
        reservationRepository.updateSchedule(reservation);
        return reservation;
    }

    @Transactional
    public Reservation changeScheduleByManager(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationSlot newSlot = getSlot(command.dateId(), command.timeId(), reservation.getSlot().getThemeId());
        validateAlreadyBookedByOthers(newSlot);

        reservation.changeScheduleByManager(newSlot, LocalDateTime.now());
        reservationRepository.updateSchedule(reservation);
        return reservation;
    }

    private void cancelByManger(Reservation cancelTarget) {
        cancelTarget.updateStatus(CANCELED);
        reservationRepository.updateStatus(cancelTarget);
    }

    private void cancel(Reservation cancelTarget, String requesterName) {
        cancelTarget.cancel(requesterName, LocalDateTime.now());
        reservationRepository.updateStatus(cancelTarget);
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
    }

    private ReservationSlot getSlot(Long dateId, Long timeId, Long themeId) {
        return reservationSlotRepository.findAvailableByDateIdTimeIdThemeId(dateId, timeId, themeId)
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
    }

    private ReservationSlot getSlotWithLock(Long dateId, Long timeId, Long themeId) {
        return reservationSlotRepository.findAvailableByDateIdTimeIdThemeIdForUpdate(dateId, timeId, themeId)
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
    }

    private void validateAlreadyBookedByOthers(ReservationSlot slot) {
        if (checkAlreadyBookedByOthers(slot)) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

    private boolean checkAlreadyBookedByOthers(ReservationSlot slot) {
        return reservationRepository.existsReservedBySlot(slot);
    }

    private Reservations findTimeSlotReservations(ReservationSlot slot) {
        return new Reservations(reservationRepository.findReservedAndWaitingBySlot(slot));
    }

}
