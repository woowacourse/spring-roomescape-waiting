package roomescape.reservation.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.repository.ReservationTimeRepository;

import java.util.List;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;

@Service
@Transactional(readOnly = true)
    @RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ThemeRepository themeRepository;

    public List<Reservation> readAll() {
        return reservationRepository.findAll();
    }

    public List<ReservationWithWaitingTurn> readAllByName(String name) {
        return reservationRepository.findMyReservationsWithWaitingTurn(name);
    }

    @Transactional
    public Reservation reserve(String name, ReservationSaveCommand command) {
        ReservationTime time = getReservationTime(command.timeId());
        time.validateIsInactive();

        ReservationDate date = getReservationDate(command.dateId());
        date.validateIsInactive();

        Theme theme = getTheme(command.themeId());
        theme.validateIsInactive();

        ReservationSlot slot = ReservationSlot.of(date, time, theme);

        Reservations reservationsOfTimeSlot = findTimeSlotReservations(slot);
        Reservation reservation = reservationsOfTimeSlot.reserve(name, slot, LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelByManager(Long id) {
        Reservation reservation = getReservation(id);
        reservation.updateStatus(CANCELED);
        reservationRepository.updateStatus(reservation);
        return reservation;
    }

    @Transactional
    public Reservation cancel(Long id, String requesterName) {
        Reservation reservation = getReservation(id);
        reservation.cancel(requesterName);
        reservationRepository.updateStatus(reservation);
        return reservation;
    }

    @Transactional
    public Reservation changeSchedule(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();

        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();

        ReservationSlot slot = ReservationSlot.of(newDate, newTime, reservation.getTheme());
        validateAlreadyBookedByOthers(slot);

        reservation.changeSchedule(command.requesterName(), newDate, newTime);
        reservationRepository.updateSchedule(reservation);
        return reservation;
    }

    @Transactional
    public Reservation changeScheduleByManager(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();

        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();

        ReservationSlot slot = ReservationSlot.of(newDate, newTime, reservation.getTheme());
        validateAlreadyBookedByOthers(slot);

        reservation.changeScheduleByManager(newDate, newTime);
        reservationRepository.updateSchedule(reservation);
        return reservation;
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException(TIME_NOT_FOUND));
    }

    private ReservationDate getReservationDate(Long dateId) {
        return reservationDateRepository.findById(dateId)
                .orElseThrow(() -> new ReservationDateException(DATE_NOT_FOUND));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeException(THEME_NOT_FOUND));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
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
