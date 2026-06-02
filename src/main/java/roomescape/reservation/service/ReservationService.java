package roomescape.reservation.service;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;
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
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
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

    private final ReservationSlotRepository reservationSlotRepository;
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

        lockSlot(command.dateId(), command.timeId(), command.themeId());

        boolean isReservedSlot = checkReserved(name, command.dateId(), command.timeId(),
            command.themeId());
        if (isReservedSlot) {
            return reservationRepository.save(
                Reservation.wait(name, reservationDate, reservationTime, theme, now));
        }
        return reservationRepository.save(
            Reservation.create(name, reservationDate, reservationTime, theme, now));
    }

    @Transactional
    public Reservation cancelByManager(Long id) {
        Reservation reservation = getReservation(id);
        reservation.cancelByManager();
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

        lockSlot(command.dateId(), command.timeId(), reservation.getTheme().getId());
        decideStatus(command, reservation);
        reservation.changeSchedule(command.requesterName(), newDate, newTime);
        reservation.changeRequestedAt(LocalDateTime.now());
        reservationRepository.updateScheduleAndStatus(reservation);
        return reservation;
    }

    @Transactional
    public Reservation changeScheduleByManager(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();
        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();
        reservation.changeScheduleByManager(newDate, newTime);

        lockSlot(command.dateId(), command.timeId(), reservation.getTheme().getId());
        decideStatus(command, reservation);
        reservation.changeRequestedAt(LocalDateTime.now());
        reservationRepository.updateScheduleAndStatus(reservation);
        return reservation;
    }

    private void lockSlot(Long dateId, Long timeId, Long themeId) {
        reservationSlotRepository.saveIfAbsent(ReservationSlot.create(dateId, timeId, themeId));
        reservationSlotRepository.lockByDateTimeAndThemeId(dateId, timeId, themeId);
    }

    private void decideStatus(ReservationChangeCommand command, Reservation reservation) {
        boolean isReservedSlot = checkReservedExcept(reservation.getId(), reservation.getName(),
            command.dateId(), command.timeId(), reservation.getTheme().getId());
        if (isReservedSlot) {
            reservation.updateStatus(WAITING);
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

    private boolean checkReserved(String name, Long dateId, Long timeId, Long themeId) {
        return checkReservedExcept(null, name, dateId, timeId, themeId);
    }

    private boolean checkReservedExcept(Long excludedId, String name, Long dateId,
        Long timeId, Long themeId) {
        List<Reservation> reservationsInSameSlot =
            reservationRepository.findAllActiveByDateTimeAndThemeId(dateId, timeId, themeId).stream()
                .filter(reservation -> !reservation.getId().equals(excludedId))
                .toList();
        validateReservedByMyself(reservationsInSameSlot, name);

        return !reservationsInSameSlot.isEmpty();
    }

    private void validateReservedByMyself(List<Reservation> reservationsInSameSlot, String name) {
        boolean reservedByMyself = reservationsInSameSlot.stream()
            .anyMatch(reservation -> reservation.isOwner(name));
        if (reservedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

}
