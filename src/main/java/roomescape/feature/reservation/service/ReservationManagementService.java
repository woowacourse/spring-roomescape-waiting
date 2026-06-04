package roomescape.feature.reservation.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.reservation.cancel.SlotReleasedEvent;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@Service
@RequiredArgsConstructor
public class ReservationManagementService implements ReservationService, AdminReservationService, WaitingService {

    private static final int MAX_CONCURRENCY_ATTEMPTS = 2;
    private static final long CONCURRENCY_BACKOFF_MILLIS = 50L;
    private static final double CONCURRENCY_BACKOFF_MULTIPLIER = 2.0;

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationMapper reservationMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<ReservationResponseDto> getReservations() {
        List<Reservation> reservations = reservationRepository.findAllReservations();
        return convertReservationsToDto(reservations);
    }

    private List<ReservationResponseDto> convertReservationsToDto(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> reservationMapper.toResponseDto(reservation, getWaitingNumber(reservation)))
                .toList();
    }

    private Integer getWaitingNumber(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            return null;
        }

        return reservationRepository.countByIdLessThanEqualAndDateAndTimeAndTheme(reservation.getId(),
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    @Override
    public List<ReservationResponseDto> getReservationsByName(ReserverName name) {
        List<Reservation> reservations = reservationRepository.findReservationsByNameAndNotDeleted(name);
        return reservations.stream()
                .map(reservation -> reservationMapper.toResponseDto(reservation, getWaitingNumber(reservation)))
                .toList();
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {DuplicateKeyException.class, OptimisticLockingFailureException.class},
            maxAttempts = MAX_CONCURRENCY_ATTEMPTS,
            backoff = @Backoff(delay = CONCURRENCY_BACKOFF_MILLIS, multiplier = CONCURRENCY_BACKOFF_MULTIPLIER)
    )
    public ReservationCreateResponseDto saveReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command, ReservationStatus.ACTIVE);

        validateNotReservedOrWaitedByOther(reservation);

        return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
    }

    private Reservation createReservation(ReservationCreateCommand command, ReservationStatus status) {
        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        Time time = timeRepository.findTimeByIdAndNotDeleted(command.timeId()).orElse(null);
        if (time == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        Theme theme = themeRepository.findThemeByIdAndNotDeleted(command.themeId()).orElse(null);
        if (theme == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralParametersException(ReservationErrorType.FIELD_RESOURCE_NOT_FOUND,
                    parameterErrorResponses);
        }

        return Reservation.create(command.name(), command.date(), time, theme, status);
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {DuplicateKeyException.class, OptimisticLockingFailureException.class},
            maxAttempts = MAX_CONCURRENCY_ATTEMPTS,
            backoff = @Backoff(delay = CONCURRENCY_BACKOFF_MILLIS, multiplier = CONCURRENCY_BACKOFF_MULTIPLIER)
    )
    public ReservationCreateResponseDto updateReservation(Long id, ReservationUpdateCommand command) {
        Reservation existingReservation = reservationRepository.findReservationByIdAndNotDeleted(id)
                .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        Time newTime = timeRepository.findTimeByIdAndNotDeleted(command.timeId())
                .orElseThrow(() -> new GeneralException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND));
        Theme newTheme = themeRepository.findThemeByIdAndNotDeleted(command.themeId())
                .orElseThrow(() -> new GeneralException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND));

        Reservation updated = existingReservation.update(command.name(), command.date(), newTime, newTheme);

        validateNotReservedOrWaitedByOther(updated);

        eventPublisher.publishEvent(new SlotReleasedEvent(
                existingReservation.getTime().getId(),
                existingReservation.getTheme().getId(),
                existingReservation.getDate()
        ));

        return reservationMapper.toCreateResponseDto(reservationRepository.update(updated));
    }

    @Override
    @Transactional
    public ReservationCancelResponseDto cancelReservation(Long id, ReserverName name) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
                .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        Reservation canceledReservation = reservation.cancelActive(name);
        int changedRowCount = reservationRepository.changeStatus(
                id, ReservationStatus.ACTIVE, ReservationStatus.CANCELED);
        if (changedRowCount == 0) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        eventPublisher.publishEvent(new SlotReleasedEvent(
                canceledReservation.getTime().getId(),
                canceledReservation.getTheme().getId(),
                canceledReservation.getDate()
        ));

        return reservationMapper.toCancelResponseDto(canceledReservation);
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {DuplicateKeyException.class, OptimisticLockingFailureException.class},
            maxAttempts = MAX_CONCURRENCY_ATTEMPTS,
            backoff = @Backoff(delay = CONCURRENCY_BACKOFF_MILLIS, multiplier = CONCURRENCY_BACKOFF_MULTIPLIER)
    )
    public void deleteReservationById(Long id) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
                .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));
        reservationRepository.update(reservation.delete());

        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            eventPublisher.publishEvent(new SlotReleasedEvent(
                    reservation.getTime().getId(),
                    reservation.getTheme().getId(),
                    reservation.getDate()
            ));
        }
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {DuplicateKeyException.class, OptimisticLockingFailureException.class},
            maxAttempts = MAX_CONCURRENCY_ATTEMPTS,
            backoff = @Backoff(delay = CONCURRENCY_BACKOFF_MILLIS, multiplier = CONCURRENCY_BACKOFF_MULTIPLIER)
    )
    public ReservationCreateResponseDto saveWaitingReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command, ReservationStatus.WAITING);

        validateNotAlreadyWaitingByMySelf(reservation);
        validateNotReservedByMyself(reservation);
        validateAlreadyReserved(reservation);

        return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationCancelResponseDto cancelWaitingReservation(Long id, ReserverName name) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
                .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        Reservation canceledReservation = reservation.cancelWaiting(name);
        int changedRowCount = reservationRepository.changeStatus(
                id, ReservationStatus.WAITING, ReservationStatus.CANCELED);
        if (changedRowCount <= 0) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        return reservationMapper.toCancelResponseDto(canceledReservation);
    }

    @Recover
    public ReservationCreateResponseDto recoverSave(DataAccessException e, ReservationCreateCommand command) {
        throw new GeneralException(ReservationErrorType.CONCURRENT_MODIFICATION);
    }

    @Recover
    public ReservationCreateResponseDto recoverUpdate(DataAccessException e, Long id, ReservationUpdateCommand command) {
        throw new GeneralException(ReservationErrorType.CONCURRENT_MODIFICATION);
    }

    @Recover
    public void recoverDelete(DataAccessException e, Long id) {
        throw new GeneralException(ReservationErrorType.CONCURRENT_MODIFICATION);
    }

    private void validateNotReservedOrWaitedByOther(Reservation reservation) {
        if (reservationRepository.existsActiveOrWaitingReservation(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme())) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private void validateNotReservedByMyself(Reservation reservation) {
        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.ACTIVE)) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private void validateNotAlreadyWaitingByMySelf(Reservation reservation) {
        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.WAITING)) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }
    }

    private void validateAlreadyReserved(Reservation reservation) {
        boolean alreadyReserved = reservationRepository.existsReservationByDateAndTimeAndThemeAndActive(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );

        if (!alreadyReserved) {
            throw new GeneralException(ReservationErrorType.NOT_RESERVED);
        }
    }
}
