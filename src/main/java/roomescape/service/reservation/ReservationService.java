package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationAvailabilityPolicy;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationValidator reservationValidator;
    private final ReservationAvailabilityPolicy reservationAvailabilityPolicy;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final ReservationValidator reservationValidator,
            final ReservationAvailabilityPolicy reservationAvailabilityPolicy
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationValidator = reservationValidator;
        this.reservationAvailabilityPolicy = reservationAvailabilityPolicy;
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(final String name, final LocalDate date, final Long themeId, final Long timeId) {
        reservationValidator.validateCreateReferenceIds(themeId, timeId);

        Theme theme = themeService.getById(themeId);
        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation nonIdReservation = createNewReservation(name, date, theme, reservationTime);

        if (reservationRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return reservationRepository.save(nonIdReservation);
    }

    public void deleteById(final long id) {
        int affectedRowCount = reservationRepository.deleteById(id);

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND, "삭제된 예약 데이터가 없습니다.");
        }
    }

    public void deleteByIdAndName(final long id, final String name) {
        reservationValidator.validateLookupName(name);

        Reservation reservation = reservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateCancelable(reservation);

        int affectedRowCount = reservationRepository.deleteById(reservation.getId());

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND, "삭제된 예약 데이터가 없습니다.");
        }
    }

    public Reservation updateByIdAndName(
            final long id,
            final String name,
            final LocalDate date,
            final Long timeId
    ) {
        reservationValidator.validateLookupName(name);
        reservationValidator.validateUpdateReferenceIds(timeId);

        Reservation reservation = reservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateUpdatable(reservation);

        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation updatedReservation = updateReservationDateAndTime(reservation, date, reservationTime);

        if (reservationRepository.existsByDateAndThemeIdAndTimeIdExcludingId(
                date,
                reservation.getTheme().getId(),
                timeId,
                reservation.getId()
        )) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return reservationRepository.update(updatedReservation);
    }

    private Reservation createNewReservation(
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime reservationTime
    ) {
        try {
            reservationAvailabilityPolicy.validateReservable(date, reservationTime, LocalDateTime.now());
            return Reservation.createNew(name, date, theme, reservationTime);
        } catch (IllegalArgumentException exception) {
            throw toInvalidInputException(exception);
        }
    }

    private Reservation updateReservationDateAndTime(
            final Reservation reservation,
            final LocalDate date,
            final ReservationTime reservationTime
    ) {
        try {
            reservationAvailabilityPolicy.validateReservable(date, reservationTime, LocalDateTime.now());
            return reservation.withDateAndTime(date, reservationTime);
        } catch (IllegalArgumentException exception) {
            throw toInvalidInputException(exception);
        }
    }

    private InvalidInputException toInvalidInputException(final IllegalArgumentException exception) {
        if (ReservationAvailabilityPolicy.PAST_RESERVATION_MESSAGE.equals(exception.getMessage())) {
            return new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, exception.getMessage());
        }

        return new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
    }
}
