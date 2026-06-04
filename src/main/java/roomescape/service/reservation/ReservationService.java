package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(final String name, final LocalDate date, final long themeId, final long timeId) {
        Theme theme = themeService.getById(themeId);
        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        ReservationSlot slot = new ReservationSlot(date, theme, reservationTime);
        Reservation nonIdReservation = createNewReservation(name, slot);

        if (reservationRepository.findBySlot(slot).isPresent()) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return reservationRepository.save(nonIdReservation);
    }

    public void deleteById(final long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "삭제된 예약 데이터가 없습니다."
                ));

        validateNoWaiting(reservation);
        reservationRepository.delete(reservation);
    }

    public void deleteByIdAndName(final long id, final String name) {
        ReservationName lookupName = ReservationName.from(name);

        Reservation reservation = reservationRepository.findByIdAndName(id, lookupName.value())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        if (!reservation.hasName(lookupName.value())) {
            throw new ResourceNotFoundException(
                    ErrorCode.MY_RESERVATION_NOT_FOUND,
                    "조회한 이름으로 찾은 예약이 없습니다."
            );
        }

        validateCancelable(reservation);
        validateNoWaiting(reservation);
        reservationRepository.delete(reservation);
    }

    public Reservation updateByIdAndName(
            final long id,
            final String name,
            final LocalDate date,
            final long timeId
    ) {
        ReservationName lookupName = ReservationName.from(name);

        Reservation reservation = reservationRepository.findByIdAndName(id, lookupName.value())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        if (!reservation.hasName(lookupName.value())) {
            throw new ResourceNotFoundException(
                    ErrorCode.MY_RESERVATION_NOT_FOUND,
                    "조회한 이름으로 찾은 예약이 없습니다."
            );
        }

        validateUpdatable(reservation);

        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation updatedReservation = updateReservationDateAndTime(reservation, date, reservationTime);

        if (reservationRepository.findBySlot(updatedReservation.getSlot())
                .filter(conflict -> !conflict.equals(reservation))
                .isPresent()) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return reservationRepository.update(updatedReservation);
    }

    private void validateCancelable(final Reservation reservation) {
        if (reservation.isPast(LocalDateTime.now())) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_CANCELLED,
                    "이미 지난 예약은 취소할 수 없습니다."
            );
        }
    }

    private void validateUpdatable(final Reservation reservation) {
        if (reservation.isPast(LocalDateTime.now())) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_UPDATED,
                    "이미 지난 예약은 변경할 수 없습니다."
            );
        }
    }

    private void validateNoWaiting(final Reservation reservation) {
        if (!reservationWaitingRepository.findLineByReservation(reservation).isEmpty()) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_HAS_WAITING,
                    "예약 대기가 존재하는 예약은 바로 삭제할 수 없습니다."
            );
        }
    }

    private Reservation createNewReservation(
            final String name,
            final ReservationSlot slot
    ) {
        try {
            return Reservation.createNew(name, date, theme, reservationTime, LocalDateTime.now());
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
            return reservation.withDateAndTime(date, reservationTime, LocalDateTime.now());
        } catch (IllegalArgumentException exception) {
            throw toInvalidInputException(exception);
        }
    }

    private InvalidInputException toInvalidInputException(final IllegalArgumentException exception) {
        if (Reservation.PAST_RESERVATION_MESSAGE.equals(exception.getMessage())) {
            return new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, exception.getMessage());
        }

        return new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
    }
}
