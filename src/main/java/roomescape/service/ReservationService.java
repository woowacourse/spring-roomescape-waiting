package roomescape.service;

import java.time.LocalTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.dto.ReservationCreateCommand;
import roomescape.dto.ReservationModifyCommand;
import roomescape.dto.AvailableDateResult;
import roomescape.dto.ReservationResult;
import roomescape.dto.ReservationTimeResult;
import roomescape.dto.ReservationTimeStatusResult;
import roomescape.dto.ThemeResult;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private static final int RESERVABLE_DAYS_RANGE = 14;

    public List<ReservationResult> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationService::mapDomainToDto)
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
        final LocalDate date = data.date();
        validateFutureOrPresentDate(date);
        final Long timeId = data.timeId();
        final ReservationTime reservationTime = getReservationTime(timeId);
        validateFutureOrPresentTime(date, reservationTime);
        final Long themeId = data.themeId();
        final Theme theme = getTheme(themeId);

        validateAvailable(date, timeId, themeId);

        final Reservation newReservation = Reservation.create(data.name(), date, reservationTime, theme);

        final Reservation savedReservation = reservationRepository.save(newReservation);
        return mapDomainToDto(savedReservation);
    }

    public void delete(final Long reservationId) {
        final boolean deleted = reservationRepository.deleteById(reservationId);

        if (!deleted) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    public void deleteWithValidation(final Long reservationId, final String name) {
        Reservation reservation = getReservation(reservationId);
        validateReservationOwner(reservation, name);
        final LocalDate date = reservation.getDate();
        validateFutureOrPresentDate(date);
        final ReservationTime reservationTime = reservation.getTime();
        validateFutureOrPresentTime(date, reservationTime);
        delete(reservationId);
    }

    public AvailableDateResult getReservationOptions() {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).toList();

        return new AvailableDateResult(dates);
    }

    public List<ReservationResult> getReservationsByName(final String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        return reservations.stream()
                .map(ReservationService::mapDomainToDto)
                .toList();
    }

    public ReservationResult modify(final ReservationModifyCommand reservationModifyCommand) {
        final Long reservationId = reservationModifyCommand.reservationId();
        final Reservation originalReservation = getReservation(reservationId);
        final String personName = reservationModifyCommand.name();
        validateReservationOwner(originalReservation, personName);

        final LocalDate originalDate = originalReservation.getDate();
        final ReservationTime originalTime = originalReservation.getTime();
        validateFutureOrPresentDate(originalDate);
        validateFutureOrPresentTime(originalDate, originalTime);

        final Long timeId = Objects.requireNonNullElse(
                reservationModifyCommand.timeId(),
                originalTime.getId()
        );
        final ReservationTime reservationTime = getReservationTime(timeId);
        final Reservation modifiedReservation = originalReservation.modify(
                reservationModifyCommand.date(),
                reservationTime
        );

        final LocalDate date = modifiedReservation.getDate();
        validateFutureOrPresentDate(date);
        validateFutureOrPresentTime(date, reservationTime);
        validateAvailable(date, timeId, modifiedReservation.getTheme().getId());

        reservationRepository.updateDateAndTime(modifiedReservation);
        return mapDomainToDto(modifiedReservation);
    }

    private static ReservationResult mapDomainToDto(final Reservation reservation) {
        final ReservationTime reservationTime = reservation.getTime();
        final Theme theme = reservation.getTheme();

        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                new ReservationTimeResult(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservationTime.getEndAt()
                ),
                new ThemeResult(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnailUrl()
                )
        );
    }

    private Reservation getReservation(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateFutureOrPresentDate(final LocalDate date) {
        final LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
    }

    private void validateFutureOrPresentTime(final LocalDate date, final ReservationTime reservationTime) {
        final LocalDate today = LocalDate.now();
        final LocalTime now = LocalTime.now();
        if (date.equals(today) && reservationTime.isBefore(now)) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
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
}
