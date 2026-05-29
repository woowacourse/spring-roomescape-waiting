package roomescape.feature.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.error.type.ReservationErrorType;
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
public class ReservationService {

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
        List<Reservation> reservations = reservationRepository.findReservationsByNotDeleted();
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

    public List<ReservationResponseDto> getReservationsByName(ReserverName name) {
        List<Reservation> reservations = reservationRepository.findReservationsByNameAndNotDeleted(name);
        return reservations.stream()
            .map(reservation -> reservationMapper.toResponseDto(reservation, getWaitingNumber(reservation)))
            .toList();
    }

    @Transactional
    public ReservationCreateResponseDto saveReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command, ReservationStatus.ACTIVE);

        if (reservationRepository.existsReservationByDateAndTimeAndThemeAndNotDeleted(reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme())) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private Reservation createReservation(ReservationCreateCommand command, ReservationStatus status) {
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

        return Reservation.create(command.name(), command.date(), time, theme, status);
    }

    @Transactional
    public ReservationCreateResponseDto updateReservation(Long id, ReservationUpdateCommand command) {
        Reservation existingReservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!existingReservation.getName().equals(command.name())) {
            throw new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);
        }

        if (existingReservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        if (existingReservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_UPDATE);
        }

        Reservation updateReservation = createUpdateReservation(existingReservation, command);
        if (reservationRepository.existsReservationByDateAndTimeAndThemeAndNotDeletedAndIdNot(
            updateReservation.getDate(), updateReservation.getTime(), updateReservation.getTheme(), id)) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.update(updateReservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private Reservation createUpdateReservation(Reservation existingReservation, ReservationUpdateCommand command) {
        LocalDate date = getUpdateDate(existingReservation, command);
        Time time = getUpdateTime(existingReservation, command);
        Theme theme = getUpdateTheme(existingReservation, command);

        validateUpdateResources(time, theme);

        return Reservation.reconstruct(existingReservation.getId(), existingReservation.getName(), date, time, theme,
            existingReservation.getStatus());
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

    private void validateUpdateResources(Time time, Theme theme) {
        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        if (time == null || time.getDeletedAt() != null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        if (theme == null || theme.getDeletedAt() != null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralParametersException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND,
                parameterErrorResponses);
        }
    }

    @Transactional
    public ReservationCancelResponseDto cancelReservation(Long id, ReserverName name) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!reservation.getName().equals(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }

        return reservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancel()));
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsReservationByIdAndNotDeleted(id)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }

        reservationRepository.deleteReservationById(id);
    }

    @Transactional
    public ReservationCreateResponseDto saveWaitingReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command, ReservationStatus.WAITING);

        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.WAITING)) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }

        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.ACTIVE)) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }
    }

    @Transactional
    public ReservationCancelResponseDto cancelWaitingReservation(Long id, ReserverName name) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!reservation.getName().equals(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }

        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }

        return reservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancel()));
    }
}
