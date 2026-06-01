package roomescape.domain.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationEditableStatus;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.reservation.mapper.ReservationMapper;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationWithWaitingNumber;
import roomescape.domain.reservation.vo.ReserverName;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
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
        List<ReservationWithWaitingNumber> reservationsWithWaitingNumbers =
            reservationRepository.findReservationsByNotDeletedWithWaitingNumber();
        return convertReservationsToDto(reservationsWithWaitingNumbers);
    }

    private List<ReservationResponseDto> convertReservationsToDto(List<ReservationWithWaitingNumber> reservations) {
        return reservations.stream()
            .map(reservationWithWaitingNumber -> {
                Reservation reservation = reservationWithWaitingNumber.reservation();
                return reservationMapper.toResponseDto(
                    reservation,
                    getEditableStatus(reservation),
                    reservationWithWaitingNumber.waitingNumber()
                );
            })
            .toList();
    }

    private ReservationEditableStatus getEditableStatus(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationEditableStatus.CANCELED;
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            return ReservationEditableStatus.LOCKED;
        }

        if (reservation.getStatus() == ReservationStatus.WAITING) {
            return ReservationEditableStatus.WAITING;
        }

        if (reservation.getTime().isDeleted() || reservation.getTheme().isDeleted()) {
            return ReservationEditableStatus.EDIT_RECOMMENDED;
        }

        return ReservationEditableStatus.EDITABLE;
    }

    public List<ReservationResponseDto> getReservationsByName(String name) {
        List<ReservationWithWaitingNumber> reservationsWithWaitingNumber =
            reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber(name);
        return convertReservationsToDto(reservationsWithWaitingNumber);
    }

    @Transactional
    public ReservationCreateResponseDto saveReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command);

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
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

        return Reservation.create(command.name(), command.date(), time, theme);
    }

    @Transactional
    public ReservationCreateResponseDto updateReservation(Long id, ReserverName name,
        ReservationUpdateCommand command) {
        Reservation existingReservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!existingReservation.getName().equals(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);
        }

        if (existingReservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        if (existingReservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_UPDATE);
        }

        Reservation updateReservation = createUpdateReservation(existingReservation, command);
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
        Reservation reservation = createReservation(command);
        Reservation waitingReservation = reservation.toWaiting();

        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.ACTIVE)) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.save(waitingReservation));
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
