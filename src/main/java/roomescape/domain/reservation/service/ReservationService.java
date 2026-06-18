package roomescape.domain.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.dto.response.ReservationTimeStartAtResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.reservation.mapper.ReservationMapper;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationWithWaitingNumber;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationMapper reservationMapper;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository,
        ThemeRepository themeRepository, ReservationMapper reservationMapper, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationMapper = reservationMapper;
        this.clock = clock;
    }

    public List<ReservationResponseDto> getReservations() {
        List<ReservationWithWaitingNumber> reservationsWithWaitingNumbers =
            reservationRepository.findReservationsByNotDeletedWithWaitingNumber();
        return convertReservationsToDto(reservationsWithWaitingNumbers);
    }

    public List<ReservationResponseDto> getReservationsByName(String name) {
        List<ReservationWithWaitingNumber> reservationsWithWaitingNumber =
            reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber(name);
        return convertReservationsToDto(reservationsWithWaitingNumber);
    }

    public ReservationTimeStartAtResponseDto getReservationTimeStartAtForSqlObservation(Long reservationId) {
        log.info("=== SQL observation: findById({}) ===", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        log.info("=== SQL observation: getTime().getStartAt() ===");
        LocalTime startAt = reservation.getTime().getStartAt();
        log.info("=== SQL observation: startAt={} ===", startAt);

        return new ReservationTimeStartAtResponseDto(reservationId, startAt);
    }

    private List<ReservationResponseDto> convertReservationsToDto(List<ReservationWithWaitingNumber> reservations) {
        return reservations.stream()
            .map(reservationWithWaitingNumber -> {
                Reservation reservation = reservationWithWaitingNumber.reservation();
                return reservationMapper.toResponseDto(
                    reservation,
                    reservation.getEditableStatus(LocalDateTime.now(clock)),
                    reservationWithWaitingNumber.waitingNumber()
                );
            })
            .toList();
    }

    @Transactional
    public ReservationCreateResponseDto saveReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command);

        try {
            Reservation savedReservation = reservationRepository.save(reservation);
            reservationRepository.flush();
            return reservationMapper.toCreateResponseDto(savedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private Reservation createReservation(ReservationCreateCommand command) {
        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        Time time = timeRepository.findTimeByIdAndDeletedAtIsNull(command.timeId()).orElse(null);
        if (time == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        Theme theme = themeRepository.findThemeByIdAndDeletedAtIsNull(command.themeId()).orElse(null);
        if (theme == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralParametersException(ReservationErrorType.FIELD_RESOURCE_NOT_FOUND,
                parameterErrorResponses);
        }

        Reservation reservation = Reservation.create(command.name(), command.date(), time, theme);
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CREATE);
        }

        return reservation;
    }

    @Transactional
    public ReservationCreateResponseDto updateReservation(Long id, String name,
        ReservationUpdateCommand command) {
        Reservation existingReservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        validateReservationCanBeUpdated(existingReservation, name);

        Reservation updateReservation = createUpdateReservation(existingReservation, command);
        try {
            ReservationCreateResponseDto responseDto = reservationMapper.toCreateResponseDto(
                reservationRepository.update(updateReservation));

            if (existingReservation.isScheduleChanged(updateReservation)) {
                approveNextWaitingReservationIfVacant(existingReservation);
            }

            return responseDto;
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private void validateReservationCanBeUpdated(Reservation existingReservation, String name) {
        if (!existingReservation.isReservedBy(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);
        }

        if (!existingReservation.isActive()) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        if (existingReservation.isPast(LocalDateTime.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_UPDATE);
        }
    }

    private Reservation createUpdateReservation(Reservation existingReservation, ReservationUpdateCommand command) {
        LocalDate date = getUpdateDate(existingReservation, command);
        Time time = getUpdateTime(existingReservation, command);
        Theme theme = getUpdateTheme(existingReservation, command);

        validateReservationUpdateFieldsExist(time, theme);

        Reservation updateReservation = Reservation.reconstruct(
            existingReservation.getId(), existingReservation.getName(), date, time, theme,
            existingReservation.getStatus(), command.version());
        if (updateReservation.isPast(LocalDateTime.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_UPDATE);
        }

        return updateReservation;
    }

    private LocalDate getUpdateDate(Reservation existingReservation, ReservationUpdateCommand command) {
        if (command.date() == null) {
            return existingReservation.getDate();
        }
        return command.date();
    }

    private Time getUpdateTime(Reservation existingReservation, ReservationUpdateCommand command) {
        if (command.timeId() == null) {
            return existingReservation.getTime();
        }
        return timeRepository.findTimeByIdAndDeletedAtIsNull(command.timeId()).orElse(null);
    }

    private Theme getUpdateTheme(Reservation existingReservation, ReservationUpdateCommand command) {
        if (command.themeId() == null) {
            return existingReservation.getTheme();
        }
        return themeRepository.findThemeByIdAndDeletedAtIsNull(command.themeId()).orElse(null);
    }

    private void validateReservationUpdateFieldsExist(Time time, Theme theme) {
        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        if (time == null || time.isDeleted()) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        if (theme == null || theme.isDeleted()) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralParametersException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND,
                parameterErrorResponses);
        }
    }

    @Transactional
    public ReservationCancelResponseDto cancelReservation(Long id, String name) {
        Reservation reservation = reservationRepository.lockReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        validateReservationCanBeCanceled(reservation, name);

        ReservationCancelResponseDto cancelResponseDto = reservationMapper.toCancelResponseDto(
            reservationRepository.update(reservation.cancel()));

        approveNextWaitingReservationIfVacant(reservation);

        return cancelResponseDto;
    }

    private void validateReservationCanBeCanceled(Reservation reservation, String name) {
        if (!reservation.isReservedBy(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }

        if (!reservation.isActive()) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        reservationRepository.deleteReservationById(id);

        approveNextWaitingReservationIfVacant(reservation);
    }

    @Transactional
    public ReservationCreateResponseDto saveWaitingReservation(ReservationCreateCommand command) {
        Reservation waitingReservation = createReservation(command).toWaiting();

        validateWaitingReservationCreationAllowed(waitingReservation);

        try {
            Reservation savedReservation = reservationRepository.save(waitingReservation);
            reservationRepository.flush();
            return reservationMapper.toCreateResponseDto(savedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }
    }

    private void validateWaitingReservationCreationAllowed(Reservation reservation) {
        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.ACTIVE)) {
            throw new GeneralException(ReservationErrorType.RESERVER_ALREADY_RESERVED);
        }

        Optional<Long> reservationId = reservationRepository.lockActiveReservationBySchedule(reservation.getSchedule());

        if (reservationId.isEmpty()) {
            throw new GeneralException(ReservationErrorType.WAITING_RESERVATION_NOT_AVAILABLE);
        }
    }

    @Transactional
    public ReservationCancelResponseDto cancelWaitingReservation(Long id, String name) {
        Reservation reservation = reservationRepository.lockReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        validateWaitingReservationCanBeCanceled(reservation, name);

        return reservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancel()));
    }

    private void validateWaitingReservationCanBeCanceled(Reservation reservation, String name) {
        if (!reservation.isReservedBy(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }

        if (!reservation.isWaiting()) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }
    }

    private void approveNextWaitingReservationIfVacant(Reservation reservation) {
        if (reservationRepository.lockActiveReservationBySchedule(reservation.getSchedule()).isPresent()) {
            return;
        }

        reservationRepository.lockFirstWaitingReservationBySchedule(reservation.getSchedule())
            .ifPresent(waiting -> reservationRepository.update(waiting.toActive()));
    }
}
