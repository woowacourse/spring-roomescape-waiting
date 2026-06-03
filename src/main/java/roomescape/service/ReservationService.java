package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.dto.ReservationCreateCommand;
import roomescape.dto.ReservationModifyCommand;
import roomescape.dto.AvailableDateResult;
import roomescape.dto.ReservationResult;
import roomescape.dto.ReservationTimeStatusResult;
import roomescape.repository.WaitingListRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingListRepository waitingListRepository;

    private static final int RESERVABLE_DAYS_RANGE = 14;

    public List<ReservationResult> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationTimeStatusResult> getReservationTimeStatuses(final LocalDate date, final Long themeId) {
        return reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, themeId)
                .stream()
                .map(reservationTimesWithStatus -> new ReservationTimeStatusResult(
                        reservationTimesWithStatus.id(),
                        reservationTimesWithStatus.startAt(),
                        reservationTimesWithStatus.reserved()
                ))
                .toList();
    }

    public ReservationResult create(final ReservationCreateCommand data) {
        Theme findTheme = findThemeOrThrow(data.themeId());
        ReservationTime findReservationTime = findReservationTimeOrThrow(data.timeId());
        validateAvailable(data.date(), findReservationTime.getId(), findTheme.getId());

        final Reservation newReservation = Reservation.create(data.name(), data.date(), findReservationTime, findTheme);
        validateReservation(newReservation, findTheme, findReservationTime);

        final Reservation savedReservation = reservationRepository.save(newReservation);
        return ReservationResult.from(savedReservation);
    }

    @Transactional
    public void deleteWithValidation(final Long reservationId, final String name) {
        Reservation reservation = findReservationOrThrow(reservationId);

        validateReservationOwner(reservation, name);
        validateReservation(reservation, reservation.getTheme(), reservation.getTime());

        deleteInternal(reservationId);
    }

    @Transactional
    public void deleteAsAdmin(final Long reservationId) {
        deleteInternal(reservationId);
    }

    private void deleteInternal(final Long reservationId) {
        Reservation findReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        reservationRepository.deleteById(findReservation.getId());

        Optional<WaitingList> findFirstWaitingList = waitingListRepository.findFirstByThemeAndDateAndTimeOrderByCreatedAtAsc(findReservation.getTheme(), findReservation.getReservationDate().getDate(), findReservation.getTime());
        if (findFirstWaitingList.isEmpty()) {
            return;
        }

        WaitingList waitingList = findFirstWaitingList.get();

        final Reservation newReservation = Reservation.create(waitingList.getName(), waitingList.getReservationDate().getDate(), waitingList.getReservationTime(), waitingList.getTheme());
        reservationRepository.save(newReservation);

        waitingListRepository.deleteById(waitingList.getId());
    }

    public AvailableDateResult getReservationOptions() {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).toList();

        return new AvailableDateResult(dates);
    }

    public List<ReservationResult> getReservationsByName(final String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public ReservationResult modify(final ReservationModifyCommand reservationModifyCommand) {
        final Long reservationId = reservationModifyCommand.reservationId();
        final Reservation originalReservation = findReservationOrThrow(reservationId);

        final String personName = reservationModifyCommand.name();
        validateReservationOwner(originalReservation, personName);

        ReservationTime findReservationTime = findReservationTimeOrThrow(reservationModifyCommand.timeId());
        Theme findTheme = findThemeOrThrow(reservationModifyCommand.themeId());

        final Reservation modifiedReservation = originalReservation.modify(
                reservationModifyCommand.date(),
                findReservationTime,
                findTheme
        );
        validateReservation(modifiedReservation, modifiedReservation.getTheme(), modifiedReservation.getTime());

        validateAvailable(modifiedReservation.getReservationDate().getDate(), modifiedReservation.getTime().getId(), modifiedReservation.getTheme().getId());

        reservationRepository.updateDateAndTime(modifiedReservation);
        return ReservationResult.from(modifiedReservation);
    }

    private void validateAvailable(final LocalDate date, final Long timeId, final Long themeId) {
        boolean isAlreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeId(
                date,
                timeId,
                themeId
        );

        if (isAlreadyReserved) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_RESERVED);
        }
    }

    private void validateReservationOwner(final Reservation reservation, final String name) {
        final String reservationOwnerName = reservation.getName();
        final boolean isUserNameMatched = reservationOwnerName.equals(name);

        if (!isUserNameMatched) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }
    }

    private void validateReservation(Reservation reservation,Theme findTheme, ReservationTime findReservationTime) {
        reservation.validateNotPast();

        validateNotDuplicated(reservation, findTheme, findReservationTime);
    }

    private void validateNotDuplicated(Reservation reservation, Theme findTheme, ReservationTime findReservationTime) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getReservationDate().getDate(), findTheme.getId(), findReservationTime.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }

    private Reservation findReservationOrThrow(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private ReservationTime findReservationTimeOrThrow(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme findThemeOrThrow(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }
}
