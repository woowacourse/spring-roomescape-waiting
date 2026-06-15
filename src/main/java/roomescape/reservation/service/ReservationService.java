package roomescape.reservation.service;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
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

        lockSlot(command.dateId(), command.timeId(), command.themeId());

        boolean isReservedSlot = checkReserved(name, command.dateId(), command.timeId(),
            command.themeId());
        if (isReservedSlot) {
            Long waitingOrder = reservationRepository.findNextWaitingOrderBySlot(command.dateId(),
                command.timeId(), command.themeId());
            return reservationRepository.save(
                Reservation.wait(name, reservationDate, reservationTime, theme, waitingOrder));
        }
        return reservationRepository.save(
            Reservation.reserved(name, reservationDate, reservationTime, theme));
    }

    @Transactional
    public Reservation cancelByManager(Long id) {
        Reservation reservation = getReservation(id);

        lockSlot(reservation.getDate().getId(), reservation.getTime().getId(),
            reservation.getTheme().getId());

        reservation = getReservation(id);
        ReservationStatus reservationStatus = reservation.getStatus();
        reservation.cancelByManager();
        reservationRepository.updateStatusAndWaitingOrder(reservation);
        if (reservationStatus == RESERVED) {
            promoteWaitingReservation(reservation.getDate().getId(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        }
        return reservation;
    }

    @Transactional
    public Reservation cancel(Long id, String requesterName) {
        Reservation reservation = getReservation(id);

        lockSlot(reservation.getDate().getId(), reservation.getTime().getId(),
            reservation.getTheme().getId());

        reservation = getReservation(id);
        ReservationStatus reservationStatus = reservation.getStatus();
        reservation.cancel(requesterName);
        reservationRepository.updateStatusAndWaitingOrder(reservation);
        if (reservationStatus == RESERVED) {
            promoteWaitingReservation(reservation.getDate().getId(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        }

        return reservation;
    }

    @Transactional
    public Reservation changeSchedule(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        Long previousDateId = reservation.getDate().getId();
        Long previousTimeId = reservation.getTime().getId();
        Long themeId = reservation.getTheme().getId();
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();
        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();

        lockSlot(previousDateId, previousTimeId, themeId);
        lockSlot(newDate.getId(), newTime.getId(), themeId);

        reservation = getReservation(command.id());
        previousDateId = reservation.getDate().getId();
        previousTimeId = reservation.getTime().getId();
        themeId = reservation.getTheme().getId();
        reservation.changeSchedule(command.requesterName(), newDate, newTime);
        decideStatus(command, reservation);
        reservationRepository.updateScheduleAndStatus(reservation);
        promoteWaitingReservation(previousDateId, previousTimeId, themeId);

        return reservation;
    }

    @Transactional
    public Reservation changeScheduleByManager(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        Long previousDateId = reservation.getDate().getId();
        Long previousTimeId = reservation.getTime().getId();
        Long themeId = reservation.getTheme().getId();
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();
        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();

        lockSlot(previousDateId, previousTimeId, themeId);
        lockSlot(newDate.getId(), newTime.getId(), themeId);

        reservation = getReservation(command.id());
        previousDateId = reservation.getDate().getId();
        previousTimeId = reservation.getTime().getId();
        themeId = reservation.getTheme().getId();
        reservation.changeScheduleByManager(newDate, newTime);
        decideStatus(command, reservation);
        reservationRepository.updateScheduleAndStatus(reservation);
        promoteWaitingReservation(previousDateId, previousTimeId, themeId);
        return reservation;
    }

    private void lockSlot(Long dateId, Long timeId, Long themeId) {
        reservationSlotRepository.saveIfAbsent(ReservationSlot.create(dateId, timeId, themeId));
        reservationSlotRepository.lockByDateTimeAndThemeId(dateId, timeId, themeId);
    }

    private void promoteWaitingReservation(Long dateId, Long timeId, Long themeId) {
        boolean hasReserved = reservationRepository.findAllActiveByDateTimeAndThemeId(dateId,
                timeId, themeId)
            .stream()
            .anyMatch(reservation -> reservation.getStatus() == RESERVED);

        if (hasReserved) {
            return;
        }
        reservationRepository.findFirstWaitingByDateTimeAndThemeId(dateId, timeId, themeId)
            .ifPresent(reservation -> {
                reservation.changeToReserved();
                reservationRepository.updateStatusAndWaitingOrder(reservation);
            });
    }

    private void decideStatus(ReservationChangeCommand command, Reservation reservation) {
        boolean isReservedSlot = checkReservedExcept(reservation.getId(), reservation.getName(),
            command.dateId(), command.timeId(), reservation.getTheme().getId());
        if (isReservedSlot) {
            Long waitingOrder = reservationRepository.findNextWaitingOrderBySlot(command.dateId(),
                command.timeId(), reservation.getTheme().getId());
            reservation.changeToWaitingWithOrder(waitingOrder);
            return;
        }
        reservation.changeToReserved();
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
            reservationRepository.findAllActiveByDateTimeAndThemeId(dateId, timeId, themeId)
                .stream()
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
