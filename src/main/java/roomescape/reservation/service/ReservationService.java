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
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
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

    public List<Reservation> readAll(ReservationStatus status) {
        return reservationRepository.findAllByStatusOptional(status);
    }

    public List<ReservationWithWaitingTurn> readAllByMemberId(Long memberId) {
        return reservationRepository.findAllByMemberIdWithWaitingTurn(memberId);
    }

    @Transactional
    public Reservation reserve(Member member, ReservationSaveCommand command) {
        ReservationDate reservationDate = getReservationDate(command.dateId());
        reservationDate.validateIsInactive();

        ReservationTime reservationTime = getReservationTime(command.timeId());
        reservationTime.validateIsInactive();

        Theme theme = getTheme(command.themeId());
        theme.validateIsInactive();

        lockSlot(reservationDate, reservationTime, theme);

        boolean isReservedSlot = checkReserved(member, reservationDate, reservationTime, theme);
        if (isReservedSlot) {
            Long waitingOrder = reservationRepository.findNextWaitingOrderByDateAndTimeAndTheme(
                reservationDate, reservationTime, theme);
            return reservationRepository.save(
                Reservation.wait(member, reservationDate, reservationTime, theme, waitingOrder));
        }
        return reservationRepository.save(
            Reservation.reserved(member, reservationDate, reservationTime, theme));
    }

    @Transactional
    public Reservation cancelByManager(Long id) {
        Reservation reservation = getReservation(id);

        lockSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme());

        reservation = getReservation(id);
        ReservationStatus reservationStatus = reservation.getStatus();
        reservation.cancelByManager();
        if (reservationStatus == RESERVED) {
            promoteWaitingReservation(reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
        }
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancel(Long id, Member requester) {
        Reservation reservation = getReservation(id);

        lockSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme());

        reservation = getReservation(id);
        ReservationStatus reservationStatus = reservation.getStatus();
        reservation.cancel(requester);
        if (reservationStatus == RESERVED) {
            promoteWaitingReservation(reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation changeSchedule(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationDate previousDate = reservation.getDate();
        ReservationTime previousTime = reservation.getTime();
        Theme theme = reservation.getTheme();
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();
        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();

        lockSlot(previousDate, previousTime, theme);
        lockSlot(newDate, newTime, theme);

        reservation = getReservation(command.id());
        previousDate = reservation.getDate();
        previousTime = reservation.getTime();
        theme = reservation.getTheme();
        reservation.changeSchedule(command.requester(), newDate, newTime);
        decideStatus(reservation);
        promoteWaitingReservation(previousDate, previousTime, theme);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation changeScheduleByManager(ReservationChangeCommand command) {
        Reservation reservation = getReservation(command.id());
        ReservationDate previousDate = reservation.getDate();
        ReservationTime previousTime = reservation.getTime();
        Theme theme = reservation.getTheme();
        ReservationTime newTime = getReservationTime(command.timeId());
        newTime.validateIsInactive();
        ReservationDate newDate = getReservationDate(command.dateId());
        newDate.validateIsInactive();

        lockSlot(previousDate, previousTime, theme);
        lockSlot(newDate, newTime, theme);

        reservation = getReservation(command.id());
        reservation.changeScheduleByManager(newDate, newTime);
        decideStatus(reservation);
        promoteWaitingReservation(previousDate, previousTime, theme);

        return reservationRepository.save(reservation);
    }

    private void lockSlot(ReservationDate reservationDate, ReservationTime reservationTime,
        Theme theme) {
        reservationSlotRepository.saveIfAbsent(reservationDate, reservationTime, theme);
        reservationSlotRepository.findByDateAndTimeAndThemeForUpdate(reservationDate, reservationTime, theme);
    }

    private void promoteWaitingReservation(ReservationDate reservationDate,
        ReservationTime reservationTime, Theme theme) {
        boolean hasReserved = reservationRepository.findAllActiveByDateAndTimeAndTheme(
                reservationDate, reservationTime, theme)
            .stream()
            .anyMatch(reservation -> reservation.getStatus() == RESERVED);

        if (hasReserved) {
            return;
        }
        reservationRepository.findFirstWaitingByDateAndTimeAndTheme(reservationDate,
                reservationTime, theme)
            .ifPresent(reservation -> {
                reservation.changeToReserved();
                reservationRepository.save(reservation);
            });
    }

    private void decideStatus(Reservation reservation) {
        boolean isReservedSlot = checkReservedExcept(reservation.getId(), reservation.getMember(),
            reservation.getDate(), reservation.getTime(), reservation.getTheme());
        if (isReservedSlot) {
            Long waitingOrder = reservationRepository.findNextWaitingOrderByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
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

    private boolean checkReserved(Member member, ReservationDate reservationDate,
        ReservationTime reservationTime, Theme theme) {
        return checkReservedExcept(null, member, reservationDate, reservationTime, theme);
    }

    private boolean checkReservedExcept(Long excludedId, Member member,
        ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        List<Reservation> reservationsInSameSlot = reservationRepository.findAllActiveByDateAndTimeAndTheme(
                reservationDate, reservationTime, theme)
            .stream()
            .filter(reservation -> !reservation.getId().equals(excludedId))
            .toList();
        validateReservedByMyself(reservationsInSameSlot, member);

        return !reservationsInSameSlot.isEmpty();
    }

    private void validateReservedByMyself(List<Reservation> reservationsInSameSlot, Member member) {
        boolean reservedByMyself = reservationsInSameSlot.stream()
            .anyMatch(reservation -> reservation.isOwner(member));
        if (reservedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

}
