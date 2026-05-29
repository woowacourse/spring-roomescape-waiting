package roomescape.reservation.service;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.domain.ReservationStatus.WAITING;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.Reservation;
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
        ReservationTime reservationTime = getReservationTime(command.timeId());
        reservationTime.validateIsInactive();

        ReservationDate reservationDate = getReservationDate(command.dateId());
        reservationDate.validateIsInactive();

        Theme theme = getTheme(command.themeId());
        theme.validateIsInactive();

        LocalDateTime now = LocalDateTime.now();

        validateAlreadyBookedByMyself(name, reservationDate.getId(), reservationTime.getId(),
            theme.getId());
        boolean isAlreadyBooked = reservationRepository.existsByDateAndTimeAndThemeId(
            reservationDate.getId(), reservationTime.getId(), theme.getId());
        if (isAlreadyBooked) {
            return reservationRepository.save(
                Reservation.wait(name, reservationDate, reservationTime, theme, now));
        }
        return reservationRepository.save(
            Reservation.create(name, reservationDate, reservationTime, theme, now));
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

        validateAlreadyBookedByMyself(reservation.getName(), command.dateId(), command.timeId(),
            reservation.getTheme().getId());

        boolean isAlreadyBooked = reservationRepository.existsByDateAndTimeAndThemeId(
            command.dateId(), command.timeId(), reservation.getTheme().getId());
        reservation.changeSchedule(command.requesterName(), newDate, newTime);
        decideStatus(reservation, isAlreadyBooked);
        reservation.changeReservedAt(LocalDateTime.now());
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

        validateAlreadyBookedByMyself(reservation.getName(), command.dateId(), command.timeId(),
            reservation.getTheme().getId());

        boolean isAlreadyBooked = reservationRepository.existsByDateAndTimeAndThemeId(
            command.dateId(), command.timeId(), reservation.getTheme().getId());
        reservation.changeScheduleByManager(newDate, newTime);
        decideStatus(reservation, isAlreadyBooked);
        reservation.changeReservedAt(LocalDateTime.now());
        reservationRepository.updateSchedule(reservation);
        return reservation;
    }

    private void decideStatus(Reservation reservation, boolean isAlreadyBooked) {
        if (isAlreadyBooked) {
            reservation.updateStatus(WAITING);
            reservationRepository.updateStatus(reservation);
        }
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

    private void validateAlreadyBookedByMyself(String name, Long dateId, Long timeId,
        Long themeId) {
        boolean isAlreadyBooked = reservationRepository.findByDateTimeAndThemeId(dateId, timeId,
                themeId)
            .stream()
            .anyMatch(reservation -> reservation.isOwner(name));
        if (isAlreadyBooked) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

}
